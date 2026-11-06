import { useAuth } from '@/stores/auth'
import { SERVIDOR } from '@/plataforma'

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.status = status
  }
}

/** Convierte la respuesta de error de FastAPI en un mensaje legible. */
async function mensajeDeError(respuesta) {
  try {
    const { detail } = await respuesta.json()
    if (typeof detail === 'string') return detail
    if (Array.isArray(detail)) {
      return detail.map((e) => e.msg.replace(/^Value error, /, '')).join('. ')
    }
  } catch {
    /* respuesta sin JSON */
  }
  return `Error ${respuesta.status}`
}

/**
 * Llama a la API. `body` se envía como JSON; `form` como FormData (para subir archivos).
 * Devuelve el JSON de la respuesta o null si no hay contenido.
 */
export async function api(ruta, { method = 'GET', body, form } = {}) {
  const auth = useAuth()
  const headers = {}
  if (auth.token) headers.Authorization = `Bearer ${auth.token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let respuesta
  try {
    respuesta = await fetch(`${SERVIDOR}/api${ruta}`, {
      method,
      headers,
      body: form ?? (body !== undefined ? JSON.stringify(body) : undefined),
    })
  } catch {
    throw new ApiError(0, 'No hay conexión. Comprueba la cobertura e inténtalo de nuevo.')
  }

  if (respuesta.status === 401 && auth.token) auth.cerrarSesion({ porElServidor: true })
  if (!respuesta.ok) throw new ApiError(respuesta.status, await mensajeDeError(respuesta))
  return respuesta.status === 204 ? null : respuesta.json()
}

/** Descarga un fichero de la API con la sesión iniciada (un enlace normal no lleva el token). */
export async function descargar(ruta) {
  const auth = useAuth()
  let respuesta
  try {
    respuesta = await fetch(`${SERVIDOR}/api${ruta}`, { headers: { Authorization: `Bearer ${auth.token}` } })
  } catch {
    throw new ApiError(0, 'No hay conexión. Comprueba la cobertura e inténtalo de nuevo.')
  }
  if (!respuesta.ok) throw new ApiError(respuesta.status, await mensajeDeError(respuesta))
  const nombre = /filename="([^"]+)"/.exec(respuesta.headers.get('Content-Disposition') ?? '')?.[1] ?? 'datos.json'
  const url = URL.createObjectURL(await respuesta.blob())
  const enlace = Object.assign(document.createElement('a'), { href: url, download: nombre })
  document.body.append(enlace)
  enlace.click()
  enlace.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export const conArchivo = (archivo) => {
  const form = new FormData()
  form.append('archivo', archivo)
  return form
}
