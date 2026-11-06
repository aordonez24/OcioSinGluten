<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { MailCheck } from 'lucide-vue-next'
import { api } from '@/api'

const route = useRoute()
const email = ref(route.query.email ?? '')
const enviado = ref(false)
const enviando = ref(false)
const error = ref('')

async function pedir() {
  error.value = ''
  enviando.value = true
  try {
    await api('/auth/recuperar', { method: 'POST', body: { email: email.value } })
    enviado.value = true
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor estrecho">
    <div v-if="!enviado">
      <h1>¿Has olvidado la contraseña?</h1>
      <p class="suave">Escribe el email con el que te registraste y te enviaremos un enlace para elegir una nueva.</p>
      <form class="formulario" @submit.prevent="pedir">
        <label>
          Email
          <input v-model="email" type="email" autocomplete="email" required autofocus />
        </label>
        <p v-if="error" class="alerta" role="alert">{{ error }}</p>
        <button class="btn btn-primario btn-bloque" :disabled="enviando">
          {{ enviando ? 'Enviando…' : 'Enviar el enlace' }}
        </button>
      </form>
      <p class="pie suave"><RouterLink to="/entrar">Volver a iniciar sesión</RouterLink></p>
    </div>

    <div v-else class="hecho" role="status">
      <MailCheck :size="40" aria-hidden="true" />
      <h1>Revisa tu correo</h1>
      <p>
        Si hay una cuenta con <strong>{{ email }}</strong>, te llegará en unos minutos un enlace para cambiar la
        contraseña. Caduca en una hora.
      </p>
      <p class="suave">¿No llega? Mira en la carpeta de spam o <button class="enlace" @click="enviado = false">pídelo otra vez</button>.</p>
    </div>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 0.5rem;
}

form {
  margin-top: 1.5rem;
}

.pie {
  text-align: center;
  margin: 1.75rem 0 0;
  padding-top: 1.5rem;
  border-top: 1px solid var(--borde);
}

.hecho svg {
  color: var(--turquesa-oscuro);
  margin-bottom: 0.75rem;
}

.hecho strong {
  overflow-wrap: anywhere;
}
</style>
