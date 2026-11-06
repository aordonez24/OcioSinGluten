import { defineStore } from 'pinia'
import { computed, ref, watch } from 'vue'

export const useAuth = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token'))
  const usuario = ref(JSON.parse(localStorage.getItem('usuario') ?? 'null'))
  // true si la sesión la ha cerrado el servidor (caducó o se cambió la contraseña en otro sitio)
  const caducada = ref(false)

  const autenticado = computed(() => Boolean(token.value && usuario.value))
  const esAdmin = computed(() => usuario.value?.rol === 'admin')

  // Se guarda de forma síncrona: si se recarga justo después de iniciar sesión, no se pierde.
  watch(token, (t) => (t ? localStorage.setItem('token', t) : localStorage.removeItem('token')), { flush: 'sync' })
  watch(usuario, (u) => localStorage.setItem('usuario', JSON.stringify(u)), { deep: true, flush: 'sync' })

  function iniciarSesion({ access_token, usuario: datos }) {
    token.value = access_token
    usuario.value = datos
    caducada.value = false
  }

  function cerrarSesion({ porElServidor = false } = {}) {
    caducada.value = porElServidor === true
    token.value = null
    usuario.value = null
    // Las copias sin conexión pueden llevar datos de esta cuenta (favoritos, perfil): se borran.
    window.caches?.delete('osg-datos')
  }

  return { token, usuario, caducada, autenticado, esAdmin, iniciarSesion, cerrarSesion }
})
