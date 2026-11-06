<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Camera, Download, LogOut, Trash2 } from 'lucide-vue-next'
import { api, conArchivo, descargar } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { useConfirmar } from '@/stores/confirmar'
import { validar } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import CampoPassword from '@/components/CampoPassword.vue'

const auth = useAuth()
const avisos = useAvisos()
const confirmar = useConfirmar()
const router = useRouter()

// Si la sesión se cierra mientras se abre la página, auth.usuario es null (y la plantilla no se muestra).
const { username, nombre, apellidos, telefono, fecha_nacimiento } = auth.usuario ?? {}
const perfil = reactive({ username, nombre, apellidos, telefono, fecha_nacimiento })
const pass = reactive({ actual: '', nueva: '', repetir: '' })
const errorPerfil = ref('')
const errorPass = ref('')
const guardando = ref('')

const erroresPerfil = computed(() => ({
  username: validar.username(perfil.username),
  telefono: validar.telefono(perfil.telefono),
  fecha_nacimiento: validar.mayorDeEdad(perfil.fecha_nacimiento),
}))
const errorNueva = computed(() =>
  pass.nueva && validar.password(pass.nueva)
    ? validar.password(pass.nueva)
    : pass.repetir && pass.repetir !== pass.nueva
      ? 'Las contraseñas no coinciden'
      : '',
)

async function ejecutar(clave, accion) {
  guardando.value = clave
  try {
    return await accion()
  } finally {
    guardando.value = ''
  }
}

async function cambiarFoto(evento) {
  const archivo = evento.target.files[0]
  evento.target.value = ''
  if (!archivo) return
  await ejecutar('foto', async () => {
    try {
      auth.usuario = await api('/usuarios/yo/foto', { method: 'PUT', form: conArchivo(archivo) })
      avisos.ok('Foto actualizada')
    } catch (e) {
      avisos.error(e)
    }
  })
}

async function quitarFoto() {
  await ejecutar('foto', async () => {
    auth.usuario = await api('/usuarios/yo/foto', { method: 'DELETE' })
    avisos.ok('Foto eliminada')
  }).catch(avisos.error)
}

async function guardarPerfil() {
  errorPerfil.value = Object.values(erroresPerfil.value).find(Boolean) ?? ''
  if (errorPerfil.value) return
  await ejecutar('perfil', async () => {
    try {
      auth.usuario = await api('/usuarios/yo', { method: 'PATCH', body: perfil })
      avisos.ok('Datos guardados')
    } catch (e) {
      errorPerfil.value = e.message
    }
  })
}

async function cambiarPassword() {
  errorPass.value = ''
  if (errorNueva.value || !pass.repetir) return
  await ejecutar('pass', async () => {
    try {
      const sesion = await api('/usuarios/yo/password', { method: 'PUT', body: { actual: pass.actual, nueva: pass.nueva } })
      auth.iniciarSesion(sesion) // las demás sesiones se cierran; esta sigue con el token nuevo
      Object.assign(pass, { actual: '', nueva: '', repetir: '' })
      avisos.ok('Contraseña cambiada. Se ha cerrado la sesión en tus otros dispositivos.')
    } catch (e) {
      errorPass.value = e.message
    }
  })
}

async function descargarDatos() {
  await ejecutar('datos', () => descargar('/usuarios/yo/datos')).catch(avisos.error)
}

function salir() {
  auth.cerrarSesion()
  router.push('/')
}

async function eliminarCuenta() {
  const ok = await confirmar.pedir({
    titulo: '¿Eliminar tu cuenta?',
    texto: 'Se borrarán tu perfil, tus opiniones, tus fotos de perfil y tu actividad. No se puede deshacer.',
    accion: 'Sí, eliminar mi cuenta',
  })
  if (!ok) return
  try {
    await api(`/usuarios/${auth.usuario.username}`, { method: 'DELETE' })
    auth.cerrarSesion()
    avisos.ok('Tu cuenta se ha eliminado. ¡Gracias por haber participado!')
    router.push('/')
  } catch (e) {
    avisos.error(e)
  }
}
</script>

<template>
  <div v-if="auth.usuario" class="contenedor medio ajustes">
    <div class="cabecera-pagina">
      <div>
        <h1>Ajustes de la cuenta</h1>
        <p>
          Así te ven los demás:
          <RouterLink :to="`/u/${auth.usuario.username}`">tu perfil público</RouterLink>
        </p>
      </div>
      <button class="btn" @click="salir"><LogOut :size="17" /> Cerrar sesión</button>
    </div>

    <section class="seccion">
      <div class="explicacion">
        <h2>Foto de perfil</h2>
        <p>Ayuda a que la comunidad te reconozca en las opiniones y en el chat.</p>
      </div>
      <div class="foto">
        <Avatar :usuario="auth.usuario" :tamano="88" />
        <div class="fila">
          <label class="btn" :aria-disabled="guardando === 'foto'">
            <Camera :size="17" /> {{ auth.usuario.foto_url ? 'Cambiar foto' : 'Subir foto' }}
            <input type="file" accept="image/*" class="solo-lectores" @change="cambiarFoto" />
          </label>
          <button v-if="auth.usuario.foto_url" class="btn btn-fantasma" @click="quitarFoto">Quitar</button>
        </div>
      </div>
    </section>

    <section class="seccion">
      <div class="explicacion">
        <h2>Datos personales</h2>
        <p>El email y el DNI no se pueden cambiar. Solo tú y el equipo de administración veis estos datos.</p>
      </div>
      <form class="formulario" @submit.prevent="guardarPerfil">
        <div class="dos-columnas">
          <label>Nombre de usuario <input v-model="perfil.username" required /></label>
          <label>Email <input :value="auth.usuario.email" disabled /></label>
          <label>Nombre <input v-model="perfil.nombre" required maxlength="50" /></label>
          <label>Apellidos <input v-model="perfil.apellidos" required maxlength="80" /></label>
          <label>Teléfono <input v-model="perfil.telefono" inputmode="tel" required /></label>
          <label>Fecha de nacimiento <input v-model="perfil.fecha_nacimiento" type="date" required /></label>
          <label>DNI <input :value="auth.usuario.dni" disabled /></label>
        </div>
        <p v-if="errorPerfil" class="alerta" role="alert">{{ errorPerfil }}</p>
        <div><button class="btn btn-primario" :disabled="guardando === 'perfil'">Guardar cambios</button></div>
      </form>
    </section>

    <section class="seccion">
      <div class="explicacion">
        <h2>Contraseña</h2>
        <p>Necesitas la actual para poner una nueva. Al cambiarla se cierra la sesión en tus otros dispositivos.</p>
      </div>
      <form class="formulario" @submit.prevent="cambiarPassword">
        <label>Contraseña actual <CampoPassword v-model="pass.actual" required /></label>
        <div class="dos-columnas">
          <label>Nueva contraseña <CampoPassword v-model="pass.nueva" autocomplete="new-password" medidor required /></label>
          <label>
            Repítela
            <CampoPassword v-model="pass.repetir" autocomplete="new-password" :invalido="!!errorNueva" required />
            <span class="error-campo">{{ errorNueva }}</span>
          </label>
        </div>
        <p v-if="errorPass" class="alerta" role="alert">{{ errorPass }}</p>
        <div>
          <button class="btn btn-primario" :disabled="guardando === 'pass' || !!errorNueva">Cambiar contraseña</button>
        </div>
      </form>
    </section>

    <section class="seccion">
      <div class="explicacion">
        <h2>Tus datos</h2>
        <p>Descarga en un fichero todo lo que guardamos de tu cuenta: datos personales, opiniones, fotos, favoritos, a quién sigues y tus mensajes del chat.</p>
      </div>
      <div>
        <button class="btn" :disabled="guardando === 'datos'" @click="descargarDatos">
          <Download :size="17" /> Descargar mis datos
        </button>
      </div>
    </section>

    <section class="seccion">
      <div class="explicacion">
        <h2>Eliminar cuenta</h2>
        <p>Se borrarán tu perfil, tus opiniones y tu actividad. No se puede deshacer.</p>
      </div>
      <div>
        <button class="btn btn-peligro" @click="eliminarCuenta"><Trash2 :size="17" /> Eliminar mi cuenta</button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.ajustes {
  width: min(1000px, 100% - 2 * var(--margen));
}

.seccion {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  gap: 1rem 3rem;
  padding-block: 2rem;
  border-top: 1px solid var(--borde);
}

.explicacion h2 {
  font-size: 1.125rem;
  margin-bottom: 0.35rem;
}

.explicacion p {
  margin: 0;
  color: var(--texto-suave);
  font-size: 0.9375rem;
}

.foto {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1.25rem;
}

@media (max-width: 760px) {
  .seccion {
    grid-template-columns: 1fr;
  }
}
</style>
