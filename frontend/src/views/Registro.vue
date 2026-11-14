<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Camera } from 'lucide-vue-next'
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
  nombre: '', apellidos: '',
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
  condiciones: aceptaCondiciones.value ? '' : 'Debes aceptar las normas de la comunidad y la política de privacidad',
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
    avisos.ok(`Te damos la bienvenida a la comunidad, ${datos.nombre}`)
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
    <div>
      <h1>Crear una cuenta</h1>
      <p class="suave intro">Con tu cuenta puedes opinar, guardar tus sitios favoritos y hablar con la comunidad.</p>

      <form class="formulario" novalidate @submit.prevent="registrarse">
        <fieldset>
          <legend>Tu cuenta</legend>
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
              <input v-model="datos.email" type="email" autocomplete="email" aria-describedby="email-ayuda" v-bind="vincular('email')" />
              <span v-if="err('email')" class="error-campo">{{ err('email') }}</span>
              <span v-else id="email-ayuda" class="ayuda">Solo lo ves tú; nunca se muestra en la comunidad.</span>
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
          <legend>Cómo te verá la comunidad</legend>
          <div class="dos-columnas">
            <label>
              Nombre
              <input v-model="datos.nombre" autocomplete="given-name" maxlength="50" v-bind="vincular('nombre')" />
              <span class="error-campo">{{ err('nombre') }}</span>
            </label>
            <label>
              <span>Apellidos <span class="opcional">(opcional)</span></span>
              <input v-model="datos.apellidos" autocomplete="family-name" maxlength="80" />
            </label>
          </div>
        </fieldset>

        <div class="condiciones">
          <p class="suave pequeno">
            Tu nombre de usuario, tu nombre, tus apellidos si los pones, tu foto y tus opiniones son públicos.
            Respeta a los demás miembros: eres responsable de lo que compartes. Puedes descargar tus datos o
            eliminar tu cuenta cuando quieras desde Ajustes.
          </p>
          <label class="casilla">
            <input v-model="aceptaCondiciones" type="checkbox" v-bind="vincular('condiciones')" />
            <span>Acepto las normas de la comunidad y la <RouterLink to="/privacidad" target="_blank">política de privacidad</RouterLink></span>
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
  margin-bottom: 2.25rem;
}

.formulario {
  gap: 2.25rem;
}

fieldset + fieldset {
  padding-top: 2rem;
  border-top: 1px solid var(--borde);
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px dashed var(--borde-fuerte);
  background: var(--niebla);
  color: var(--texto-suave);
  cursor: pointer;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-boton:hover,
.avatar-boton:focus-within {
  border-color: var(--turquesa-oscuro);
  color: var(--turquesa-oscuro);
}

.avatar-boton img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.condiciones {
  display: grid;
  gap: 0.6rem;
  padding-top: 2rem;
  border-top: 1px solid var(--borde);
}

.pie {
  text-align: center;
  margin: 1.75rem 0 0;
}
</style>
