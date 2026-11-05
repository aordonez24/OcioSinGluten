import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * Diálogo de confirmación propio (sustituye a window.confirm).
 *   if (await confirmar.pedir({ titulo: '¿Borrar?', texto: '...', accion: 'Borrar' })) { ... }
 */
export const useConfirmar = defineStore('confirmar', () => {
  const abierto = ref(false)
  const opciones = ref({})
  let resolver = null

  function pedir({ titulo, texto = '', accion = 'Confirmar', peligro = true }) {
    opciones.value = { titulo, texto, accion, peligro }
    abierto.value = true
    return new Promise((r) => (resolver = r))
  }

  function responder(valor) {
    abierto.value = false
    resolver?.(valor)
    resolver = null
  }

  return { abierto, opciones, pedir, responder }
})
