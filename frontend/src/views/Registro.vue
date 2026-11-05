<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Camera, ShieldCheck } from 'lucide-vue-next'
import { api, conArchivo } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { validar } from '@/utils'
import CampoPassword from '@/components/CampoPassword.vue'

const auth = useAuth()
const avisos = useAvisos()
const router = useRouter()
const route = useRoute()

const datos = reactive({
  username: '', email: '', password: '',
  nombre: '', apellidos: '', dni: '', telefono: '', fecha_nacimiento: '',
})
const confirmacion = ref('')
const aceptaCondiciones = ref(false)
const foto = ref(null)
const previsualizacion = ref('')
const tocados = reactive(new Set()) // campos que el usuario ya ha rellenado
const intentado = ref(false)
const enviando = ref(false)
const error = ref('')

const errores = computed(() => ({
  username: validar.username(datos.username),
  email: validar.email(datos.email),
  password: validar.password(datos.password),
  confirmacion: confirmacion.value === datos.password ? '' : 'Las contraseñas no coinciden',
  nombre: datos.nombre.trim() ? '' : 'Obligatorio',
  apellidos: datos.apellidos.trim() ? '' : 'Obligatorio',
  dni: validar.dni(datos.dni),
  telefono: validar.telefono(datos.telefono),
  fecha_nacimiento: validar.mayorDeEdad(datos.fecha_nacimiento),
  condiciones: aceptaCondiciones.value ? '' : 'Debes aceptar las condiciones',
}))
// Los errores se muestran al salir de cada campo, no mientras se escribe por primera vez.
const err = (campo) => (intentado.value || tocados.has(campo) ? errores.value[campo] : '')
const tocar = (campo) => tocados.add(campo)
const vincular = (campo) => ({ 'aria-invalid': !!err(campo), onBlur: () => tocar(campo) })

function elegirFoto(evento) {
  const archivo = evento.target.files[0]
  if (!archivo) return
  foto.value = archivo
  previsualizacion.value = URL.createObjectURL(archivo)
}

async function registrarse() {
  intentado.value = true
  error.value = ''
  if (Object.values(errores.value).some(Boolean)) {
    document.querySelector('[aria-invalid="true"]')?.focus()
    return
  }

  enviando.value = true
  try {
    auth.iniciarSesion(await api('/auth/registro', { method: 'POST', body: datos }))
    if (foto.value) {
      try {
        auth.usuario = await api('/usuarios/yo/foto', { method: 'PUT', form: conArchivo(foto.value) })
      } catch (e) {
        avisos.error(`Tu cuenta se ha creado, pero no se pudo subir la foto: ${e.message}`)
      }
    }
    avisos.ok(`¡Bienvenido/a a la comunidad, ${datos.nombre}!`)
    router.push(route.query.volver || '/establecimientos')
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor medio">
    <div class="tarjeta">
      <h1>Únete a Ocio Sin Gluten</h1>
      <p class="suave intro">Crea tu cuenta para opinar, guardar tus sitios favoritos y hablar con la comunidad.</p>

      <form class="formulario" novalidate @submit.prevent="registrarse">
        <fieldset>
          <legend><span class="paso">1</span> Tu cuenta</legend>
          <div class="avatar-selector">
            <label class="avatar-boton" :title="foto ? 'Cambiar foto' : 'Añadir foto'">
              <img v-if="previsualizacion" :src="previsualizacion" alt="" />
              <Camera v-else :size="26" />
              <input type="file" accept="image/*" class="solo-lectores" @change="elegirFoto" />
            </label>
            <div>
              <strong>Foto de perfil</strong>
              <p class="suave pequeno">Opcional. Ayuda a que la comunidad te reconozca.</p>
            </div>
          </div>
          <div class="dos-columnas">
            <label>
              Nombre de usuario
              <input v-model="datos.username" autocomplete="username" placeholder="p. ej. lucia_sg" v-bind="vincular('username')" />
              <span class="error-campo">{{ err('username') }}</span>
            </label>
            <label>
              Email
              <input v-model="datos.email" type="email" autocomplete="email" v-bind="vincular('email')" />
              <span class="error-campo">{{ err('email') }}</span>
            </label>
            <label>
              Contraseña
              <CampoPassword v-model="datos.password" autocomplete="new-password" medidor :invalido="!!err('password')" @blur="tocar('password')" />
            </label>
            <label>
              Repite la contraseña
              <CampoPassword v-model="confirmacion" autocomplete="new-password" :invalido="!!err('confirmacion')" @blur="tocar('confirmacion')" />
              <span class="error-campo">{{ err('confirmacion') }}</span>
            </label>
          </div>
        </fieldset>

        <fieldset>
          <legend><span class="paso">2</span> Datos personales</legend>
          <p class="privacidad pequeno">
            <ShieldCheck :size="18" /> Estos datos son privados: solo los ves tú y el equipo de administración.
          </p>
          <div class="dos-columnas">
            <label>
              Nombre
              <input v-model="datos.nombre" autocomplete="given-name" maxlength="50" v-bind="vincular('nombre')" />
              <span class="error-campo">{{ err('nombre') }}</span>
            </label>
            <label>
              Apellidos
              <input v-model="datos.apellidos" autocomplete="family-name" maxlength="80" v-bind="vincular('apellidos')" />
              <span class="error-campo">{{ err('apellidos') }}</span>
            </label>
            <label>
              DNI
              <input v-model="datos.dni" maxlength="9" placeholder="12345678Z" v-bind="vincular('dni')" />
              <span class="error-campo">{{ err('dni') }}</span>
            </label>
            <label>
              Teléfono
              <input v-model="datos.telefono" inputmode="tel" autocomplete="tel" placeholder="600 123 123" v-bind="vincular('telefono')" />
              <span class="error-campo">{{ err('telefono') }}</span>
            </label>
            <label>
              Fecha de nacimiento
              <input v-model="datos.fecha_nacimiento" type="date" autocomplete="bday" v-bind="vincular('fecha_nacimiento')" />
              <span class="error-campo">{{ err('fecha_nacimiento') }}</span>
            </label>
          </div>
        </fieldset>

        <div class="condiciones">
          <details>
            <summary>Leer las condiciones del servicio</summary>
            <p class="suave">
              Al registrarte aceptas respetar a los demás miembros y las normas de la comunidad, y eres
              responsable del contenido que compartes. Tus datos personales nunca se muestran a otros
              usuarios. Puedes eliminar tu cuenta y todos tus datos en cualquier momento desde Ajustes.
            </p>
          </details>
          <label class="casilla">
            <input v-model="aceptaCondiciones" type="checkbox" />
            He leído y acepto las condiciones del servicio
          </label>
          <span class="error-campo">{{ err('condiciones') }}</span>
        </div>

        <p v-if="error" class="alerta" role="alert">{{ error }}</p>
        <button class="btn btn-primario btn-bloque" :disabled="enviando">
          {{ enviando ? 'Creando tu cuenta…' : 'Crear cuenta' }}
        </button>
      </form>
      <p class="pie suave">
        ¿Ya tienes cuenta? <RouterLink :to="{ path: '/entrar', query: route.query }">Inicia sesión</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.intro {
  margin-top: 0;
  margin-bottom: 1.75rem;
}

.formulario {
  gap: 2rem;
}

.avatar-selector {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar-selector p {
  margin: 0.15rem 0 0;
}

.avatar-boton {
  display: grid;
  place-items: center;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  border: 2px dashed var(--crema-3);
  background: var(--crema);
  color: var(--texto-suave);
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-boton:hover {
  border-color: var(--coral);
  color: var(--coral);
}

.avatar-boton img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.privacidad {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: -0.25rem 0 0;
  padding: 0.6rem 0.85rem;
  border-radius: var(--radio-sm);
  background: #e3f3e8;
  color: var(--verde);
  font-weight: 500;
}

.condiciones {
  display: grid;
  gap: 0.6rem;
}

summary {
  cursor: pointer;
  font-weight: 600;
  color: var(--coral-oscuro);
}

.pie {
  text-align: center;
  margin: 1.25rem 0 0;
}
</style>
