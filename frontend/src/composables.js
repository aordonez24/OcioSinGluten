import { ref } from 'vue'

const CLAVE = 'ubicacion'
const ubicacion = ref(JSON.parse(sessionStorage.getItem(CLAVE) ?? 'null'))

/** Ubicación del navegador (se pide una vez por sesión y se recuerda). */
export function useUbicacion() {
  const buscando = ref(false)

  function pedir() {
    if (ubicacion.value) return Promise.resolve(ubicacion.value)
    if (!navigator.geolocation) return Promise.reject(new Error('Tu navegador no permite obtener la ubicación'))
    buscando.value = true
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        ({ coords }) => {
          ubicacion.value = { latitud: coords.latitude, longitud: coords.longitude }
          sessionStorage.setItem(CLAVE, JSON.stringify(ubicacion.value))
          buscando.value = false
          resolve(ubicacion.value)
        },
        () => {
          buscando.value = false
          reject(new Error('No hemos podido obtener tu ubicación. Revisa los permisos del navegador.'))
        },
        { enableHighAccuracy: false, timeout: 10000, maximumAge: 10 * 60 * 1000 },
      )
    })
  }

  return { ubicacion, buscando, pedir }
}
