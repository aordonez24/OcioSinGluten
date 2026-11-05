import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

export const useAuth = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const usuario = ref(JSON.parse(localStorage.getItem('usuario') ?? 'null'))

  const autenticado = computed(() => Boolean(token.value && usuario.value))
  const esAdmin = computed(() => usuario.value?.rol === 'admin')

  watch(token, (t) => (t ? localStorage.setItem('token', t) : localStorage.removeItem('token')))
  watch(usuario, (u) => localStorage.setItem('usuario', JSON.stringify(u)), { deep: true })

  function iniciarSesion({ access_token, usuario: datos }) {
    token.value = access_token
    usuario.value = datos
  }

  function cerrarSesion() {
    token.value = null
    usuario.value = null
  }

  return { token, usuario, autenticado, esAdmin, iniciarSesion, cerrarSesion }
})
