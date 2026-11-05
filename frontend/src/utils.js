// Validaciones (las mismas reglas que aplica el backend, que es quien manda).

const LETRAS_DNI = 'TRWAGMYFPDXBNJZSQVHLCKE'

export const validar = {
  dni(v) {
    const dni = v.trim().toUpperCase()
    return /^\d{8}[A-Z]$/.test(dni) && LETRAS_DNI[Number(dni.slice(0, 8)) % 23] === dni[8]
      ? ''
      : 'DNI no válido (revisa la letra)'
  },
  telefono: (v) => (/^\d{9}$/.test(String(v).replace(/[\s-]/g, '')) ? '' : 'Debe tener 9 dígitos'),
  email: (v) => (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? '' : 'Email no válido'),
  password: (v) =>
    v.length >= 8 && /[A-Z]/.test(v) && /\d/.test(v)
      ? ''
      : 'Mínimo 8 caracteres, con una mayúscula y un número',
  codPostal: (v) => (/^\d{5}$/.test(v) ? '' : 'Debe tener 5 dígitos'),
  username: (v) => (/^[A-Za-z0-9_.-]{3,30}$/.test(v) ? '' : 'Entre 3 y 30 letras, números, _ . -'),
  mayorDeEdad(v) {
    if (!v) return 'Obligatorio'
    const nacimiento = new Date(v)
    const hoy = new Date()
    let edad = hoy.getFullYear() - nacimiento.getFullYear()
    const m = hoy.getMonth() - nacimiento.getMonth()
    if (m < 0 || (m === 0 && hoy.getDate() < nacimiento.getDate())) edad--
    return edad >= 18 ? '' : 'Debes ser mayor de edad'
  },
}

/** Pasa a minúsculas y quita tildes, para buscar "jaen" y encontrar "Jaén". */
export const normalizar = (texto = '') =>
  texto.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '')

export const coincide = (consulta, ...campos) => {
  const q = normalizar(consulta.trim())
  return !q || campos.some((c) => normalizar(c).includes(q))
}

const formatoFecha = new Intl.DateTimeFormat('es-ES', { dateStyle: 'long' })
const relativo = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })

export const fecha = (iso) => formatoFecha.format(new Date(iso))

/** "Hoy", "Ayer" o la fecha, para agrupar listas por día. */
export function etiquetaDia(iso) {
  const dia = new Date(iso)
  const hoy = new Date()
  const ayer = new Date(hoy)
  ayer.setDate(hoy.getDate() - 1)
  if (dia.toDateString() === hoy.toDateString()) return 'Hoy'
  if (dia.toDateString() === ayer.toDateString()) return 'Ayer'
  return formatoFecha.format(dia)
}

export function agruparPorDia(lista, campo = 'creado_en') {
  const grupos = []
  for (const elemento of lista) {
    const etiqueta = etiquetaDia(elemento[campo])
    if (grupos.at(-1)?.etiqueta !== etiqueta) grupos.push({ etiqueta, elementos: [] })
    grupos.at(-1).elementos.push(elemento)
  }
  return grupos
}

export function hace(iso) {
  const segundos = (new Date(iso) - Date.now()) / 1000
  const unidades = [
    ['year', 31536000],
    ['month', 2592000],
    ['week', 604800],
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60],
  ]
  for (const [unidad, s] of unidades) {
    if (Math.abs(segundos) >= s) return relativo.format(Math.round(segundos / s), unidad)
  }
  return 'ahora mismo'
}
