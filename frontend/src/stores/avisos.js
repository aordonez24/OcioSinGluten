import { defineStore } from 'pinia'
import { ref } from 'vue'

/** Notificaciones flotantes (sustituyen a los alert() de la versión anterior). */
export const useAvisos = defineStore('avisos', () => {
  const lista = ref([])
  let siguienteId = 0

  function mostrar(texto, tipo = 'ok') {
    const id = siguienteId++
    lista.value.push({ id, texto, tipo })
    setTimeout(() => cerrar(id), 4500)
  }

  function cerrar(id) {
    lista.value = lista.value.filter((a) => a.id !== id)
  }

  return {
    lista,
    cerrar,
    ok: (texto) => mostrar(texto, 'ok'),
    error: (errorOTexto) => mostrar(errorOTexto?.message ?? String(errorOTexto), 'error'),
  }
})
