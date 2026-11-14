/*
 * Service worker de Ocio Sin Gluten: permite abrir la app sin conexión.
 *
 * - La app (HTML, JS, CSS, iconos) se guarda al instalarse; la lista de ficheros y la versión
 *   las rellena Vite al compilar (ver vite.config.js).
 * - Las consultas a la API van primero a la red y guardan copia; sin cobertura se usa la copia.
 *   Si la red tarda mucho y hay copia, se muestra la copia para no dejar la pantalla en blanco.
 * - Las fotos se guardan según se ven.
 * - Nunca se guardan datos privados (sesión, notificaciones, bandeja de administración, chat).
 */
const VERSION = '__VERSION__'
const ARCHIVOS = __ARCHIVOS__

const CACHE_APP = `osg-app-${VERSION}`
const CACHE_DATOS = 'osg-datos'
const MAX_FOTOS = 150
const ESPERA_RED_MS = 4000
const API_PRIVADA = ['/api/auth', '/api/notificaciones', '/api/consultas', '/api/usuarios/yo', '/api/chat']

self.addEventListener('install', (evento) => {
  evento.waitUntil(
    caches
      .open(CACHE_APP)
      .then((cache) => cache.addAll(ARCHIVOS))
      .then(() => self.skipWaiting()),
  )
})

self.addEventListener('activate', (evento) => {
  evento.waitUntil(
    caches
      .keys()
      .then((nombres) => Promise.all(nombres.filter((n) => n.startsWith('osg-app-') && n !== CACHE_APP).map((n) => caches.delete(n))))
      .then(() => self.clients.claim()),
  )
})

const sinConexion = () =>
  new Response(JSON.stringify({ detail: 'No hay conexión. Comprueba la cobertura e inténtalo de nuevo.' }), {
    status: 503,
    headers: { 'Content-Type': 'application/json' },
  })

async function redPrimero(peticion) {
  const cache = await caches.open(CACHE_DATOS)
  const red = fetch(peticion).then((respuesta) => {
    if (respuesta.ok) cache.put(peticion, respuesta.clone())
    return respuesta
  })
  const copia = await cache.match(peticion)
  if (!copia) return red.catch(sinConexion)
  const espera = new Promise((resolver) => setTimeout(() => resolver(copia), ESPERA_RED_MS))
  return Promise.race([red.catch(() => copia), espera])
}

async function cacheConLimite(peticion) {
  const cache = await caches.open(CACHE_DATOS)
  const copia = await cache.match(peticion)
  if (copia) return copia
  const respuesta = await fetch(peticion)
  if (respuesta.ok) {
    await cache.put(peticion, respuesta.clone())
    const fotos = (await cache.keys()).filter((p) => new URL(p.url).pathname.startsWith('/uploads/'))
    await Promise.all(fotos.slice(0, Math.max(0, fotos.length - MAX_FOTOS)).map((p) => cache.delete(p)))
  }
  return respuesta
}

async function appPrimero(peticion) {
  const copia = await caches.match(peticion)
  if (copia) return copia
  const respuesta = await fetch(peticion)
  if (respuesta.ok) (await caches.open(CACHE_APP)).put(peticion, respuesta.clone())
  return respuesta
}

self.addEventListener('fetch', (evento) => {
  const { request } = evento
  const url = new URL(request.url)
  if (request.method !== 'GET' || url.origin !== self.location.origin) return

  if (url.pathname.startsWith('/api/')) {
    if (!API_PRIVADA.some((ruta) => url.pathname.startsWith(ruta))) evento.respondWith(redPrimero(request))
    return
  }
  if (request.mode === 'navigate') {
    // Es una SPA: cualquier página se sirve con index.html, de la red o de la copia.
    evento.respondWith(fetch(request).catch(() => caches.match('/')))
    return
  }
  if (url.pathname.startsWith('/uploads/')) evento.respondWith(cacheConLimite(request))
  else if (url.pathname.startsWith('/assets/') || ARCHIVOS.includes(url.pathname)) evento.respondWith(appPrimero(request))
})
