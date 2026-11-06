import { ref } from 'vue'
import { obtenerPosicion } from '@/plataforma'

const CLAVE = 'ubicacion'
const ubicacion = ref(JSON.parse(sessionStorage.getItem(CLAVE) ?? 'null'))

/** Ubicación del navegador (se pide una vez por sesión y se recuerda). */
export function useUbicacion() {
  const buscando = ref(false)

  async function pedir() {
    if (ubicacion.value) return ubicacion.value
    buscando.value = true
    try {
      const coords = await obtenerPosicion()
      ubicacion.value = { latitud: coords.latitude, longitud: coords.longitude }
      sessionStorage.setItem(CLAVE, JSON.stringify(ubicacion.value))
      return ubicacion.value
    } catch (e) {
      if (e?.message === 'Tu navegador no permite obtener la ubicación') throw e
      throw new Error('No hemos podido obtener tu ubicación. Revisa los permisos de ubicación.')
    } finally {
      buscando.value = false
    }
  }

  return { ubicacion, buscando, pedir }
}
