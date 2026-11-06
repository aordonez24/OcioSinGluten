// Diferencias entre la web y la app nativa (Android/iOS con Capacitor).
//
// En la web, el frontend y la API comparten dirección y las rutas son relativas ("/api", "/uploads").
// En la app, el frontend va dentro del paquete (https://localhost) y la API está en el servidor
// público, que se indica al compilar con VITE_SERVIDOR (consulta frontend/.env.movil).
import { Capacitor } from '@capacitor/core'

export const esNativa = Capacitor.isNativePlatform()

/** Dirección del servidor; vacía en la web (mismo origen). */
export const SERVIDOR = (import.meta.env.VITE_SERVIDOR ?? '').replace(/\/$/, '')

/** Dirección pública de la web: la que se comparte (en la app, location sería https://localhost). */
export const URL_PUBLICA = SERVIDOR || location.origin

/** "/uploads/x.webp" → dirección completa en la app; en la web se queda igual. */
export const recurso = (ruta) => (ruta?.startsWith('/') ? SERVIDOR + ruta : ruta)

/** Dirección del WebSocket del chat. */
export function urlSocket(ruta) {
  return (SERVIDOR || location.origin).replace(/^http/, 'ws') + ruta
}

/** Vibración corta al marcar algo (me gusta, favorito…). En la web solo si el móvil lo permite. */
export async function vibrar() {
  if (esNativa) {
    const { Haptics, ImpactStyle } = await import('@capacitor/haptics')
    return Haptics.impact({ style: ImpactStyle.Light }).catch(() => {})
  }
  navigator.vibrate?.(10)
}

/** Comparte un enlace con la hoja nativa del sistema; si no hay, lo copia al portapapeles. */
export async function compartir({ titulo, texto, ruta }) {
  const url = URL_PUBLICA + ruta
  if (esNativa) {
    const { Share } = await import('@capacitor/share')
    await Share.share({ title: titulo, text: texto, url, dialogTitle: 'Compartir' }).catch(() => {})
    return 'compartido'
  }
  if (navigator.share) {
    await navigator.share({ title: titulo, text: texto, url }).catch(() => {})
    return 'compartido'
  }
  await navigator.clipboard.writeText(url)
  return 'copiado'
}

/** Ubicación actual: con el GPS nativo en la app y con la API del navegador en la web. */
export async function obtenerPosicion() {
  if (esNativa) {
    const { Geolocation } = await import('@capacitor/geolocation')
    const { coords } = await Geolocation.getCurrentPosition({ enableHighAccuracy: false, timeout: 10000 })
    return coords
  }
  if (!navigator.geolocation) throw new Error('Tu navegador no permite obtener la ubicación')
  return new Promise((resolve, reject) =>
    navigator.geolocation.getCurrentPosition(({ coords }) => resolve(coords), reject, {
      enableHighAccuracy: false,
      timeout: 10000,
      maximumAge: 10 * 60 * 1000,
    }),
  )
}

/**
 * Integración con el sistema en la app nativa: botón "atrás" de Android, barra de estado y
 * pantalla de bienvenida. En la web no hace nada.
 */
export async function iniciarApp(router) {
  if (!esNativa) return
  document.documentElement.classList.add('app-nativa')
  const [{ App }, { SplashScreen }, { SystemBars, SystemBarsStyle }] = await Promise.all([
    import('@capacitor/app'),
    import('@capacitor/splash-screen'),
    import('@capacitor/core'),
  ])
  SystemBars.setStyle({ style: SystemBarsStyle.Light }).catch(() => {}) // iconos oscuros: la cabecera es blanca

  App.addListener('backButton', ({ canGoBack }) => {
    // Primero se cierra lo que esté abierto encima: un diálogo...
    const dialogo = document.querySelector('dialog[open]')
    if (dialogo) {
      const cancelar = new Event('cancel', { cancelable: true })
      if (dialogo.dispatchEvent(cancelar)) dialogo.close() // si nadie lo gestiona, se cierra sin más
      return
    }
    // ...el visor de fotos o un menú desplegable (se cierran con Escape, como con el teclado)...
    if (document.querySelector('.visor, .disparador[aria-expanded="true"], .campana [aria-expanded="true"]')) {
      document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }))
      return
    }
    // ...y si no hay nada, se vuelve atrás; desde la portada se sale de la app.
    if (canGoBack && router.currentRoute.value.path !== '/') router.back()
    else App.exitApp()
  })

  await router.isReady()
  SplashScreen.hide({ fadeOutDuration: 200 }).catch(() => {})
}
