<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { validar } from '@/utils'
import CampoPassword from '@/components/CampoPassword.vue'

const auth = useAuth()
const avisos = useAvisos()
const route = useRoute()
const router = useRouter()

// El token se quita de la barra de direcciones para que no quede en el historial.
const token = route.query.token ?? ''
if (token) router.replace({ query: {} })

const password = ref('')
const repetir = ref('')
const intentado = ref(false)
const enviando = ref(false)
const error = ref('')
const caducado = ref(!token)

const errores = computed(() => ({
  password: validar.password(password.value),
  repetir: repetir.value === password.value ? '' : 'Las contraseñas no coinciden',
}))
const err = (campo) => (intentado.value ? errores.value[campo] : '')

async function guardar() {
  intentado.value = true
  if (Object.values(errores.value).some(Boolean)) return
  error.value = ''
  enviando.value = true
  try {
    auth.iniciarSesion(await api('/auth/restablecer', { method: 'POST', body: { token, password: password.value } }))
    avisos.ok('Contraseña cambiada. Ya has iniciado sesión.')
    router.push('/establecimientos')
  } catch (e) {
    if (e.status === 400) caducado.value = true
    else error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor estrecho">
    <div v-if="caducado">
      <h1>Este enlace ya no sirve</h1>
      <p>Los enlaces para cambiar la contraseña caducan en una hora y solo se pueden usar una vez.</p>
      <RouterLink to="/recuperar" class="btn btn-primario">Pedir un enlace nuevo</RouterLink>
    </div>

    <div v-else>
      <h1>Elige una contraseña nueva</h1>
      <form class="formulario" novalidate @submit.prevent="guardar">
        <label>
          Contraseña nueva
          <CampoPassword v-model="password" autocomplete="new-password" medidor :invalido="!!err('password')" required autofocus />
          <span class="error-campo">{{ err('password') }}</span>
        </label>
        <label>
          Repite la contraseña
          <CampoPassword v-model="repetir" autocomplete="new-password" :invalido="!!err('repetir')" required />
          <span class="error-campo">{{ err('repetir') }}</span>
        </label>
        <p v-if="error" class="alerta" role="alert">{{ error }}</p>
        <button class="btn btn-primario btn-bloque" :disabled="enviando">
          {{ enviando ? 'Guardando…' : 'Guardar y entrar' }}
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
form {
  margin-top: 1.5rem;
}
</style>
