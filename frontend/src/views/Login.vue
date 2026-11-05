<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import CampoPassword from '@/components/CampoPassword.vue'
import logo from '@/assets/logo.webp'

const auth = useAuth()
const avisos = useAvisos()
const router = useRouter()
const route = useRoute()

const datos = reactive({ email: '', password: '' })
const error = ref('')
const enviando = ref(false)

async function entrar() {
  error.value = ''
  enviando.value = true
  try {
    auth.iniciarSesion(await api('/auth/login', { method: 'POST', body: datos }))
    avisos.ok(`Hola de nuevo, ${auth.usuario.nombre}`)
    router.push(route.query.volver || '/establecimientos')
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor estrecho">
    <div class="acceso">
      <div class="cabecera">
        <img :src="logo" alt="" width="72" height="72" />
        <h1>Iniciar sesión</h1>
        <p class="suave">Entra para opinar, guardar tus sitios y hablar con la comunidad.</p>
      </div>
      <form class="formulario" @submit.prevent="entrar">
        <label>
          Email
          <input v-model="datos.email" type="email" autocomplete="email" required autofocus />
        </label>
        <label>
          Contraseña
          <CampoPassword v-model="datos.password" required />
        </label>
        <p v-if="error" class="alerta" role="alert">{{ error }}</p>
        <button class="btn btn-primario btn-bloque" :disabled="enviando">
          {{ enviando ? 'Entrando…' : 'Entrar' }}
        </button>
      </form>
      <p class="pie suave">
        ¿Aún no tienes cuenta?
        <RouterLink :to="{ path: '/registro', query: route.query }">Crea una cuenta nueva</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.acceso {
  padding-block: 1rem;
}

.cabecera {
  text-align: center;
  margin-bottom: 2rem;
}

.cabecera img {
  margin: 0 auto 1rem;
  border-radius: 50%;
}

.cabecera h1 {
  margin-bottom: 0.35rem;
}

.cabecera p {
  margin: 0;
}

.pie {
  text-align: center;
  margin: 1.75rem 0 0;
  padding-top: 1.5rem;
  border-top: 1px solid var(--borde);
}
</style>
