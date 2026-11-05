import { useAuth } from '@/stores/auth'

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

  const respuesta = await fetch(`/api${ruta}`, {
    method,
    headers,
    body: form ?? (body !== undefined ? JSON.stringify(body) : undefined),
  })

  if (respuesta.status === 401 && auth.token) auth.cerrarSesion()
  if (!respuesta.ok) throw new ApiError(respuesta.status, await mensajeDeError(respuesta))
  return respuesta.status === 204 ? null : respuesta.json()
}

export const conArchivo = (archivo) => {
  const form = new FormData()
  form.append('archivo', archivo)
  return form
}
