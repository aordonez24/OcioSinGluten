// Horario semanal de un establecimiento: 7 listas (lunes a domingo) de franjas { abre, cierra }.
// Una franja cuyo cierre es anterior a la apertura termina de madrugada, al día siguiente.
import { onBeforeUnmount, ref } from 'vue'

export const DIAS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo']

const MIN_DIA = 24 * 60
const MIN_SEMANA = 7 * MIN_DIA
const PROVINCIAS_CANARIAS = ['Las Palmas', 'Santa Cruz de Tenerife']

const minutos = (hora) => {
  const [h, m] = hora.split(':').map(Number)
  return h * 60 + m
}

/** "13:00" → "13:00", "09:30" → "9:30", "00:00" → "medianoche". */
export function formatoHora(hora) {
  if (hora === '00:00') return 'medianoche'
  return hora.replace(/^0(\d)/, '$1')
}

/** "a las 13:00", "a la 1:30", "a medianoche". */
function aLas(hora) {
  const texto = formatoHora(hora)
  if (texto === 'medianoche') return 'a medianoche'
  return texto.startsWith('1:') ? `a la ${texto}` : `a las ${texto}`
}

const hasta = (hora) => aLas(hora).replace(/^a /, 'hasta ')

const formatos = {}

/** Día de la semana (0 = lunes) y minuto del día en la zona horaria del sitio. */
export function momentoLocal(fecha, provincia) {
  const zona = PROVINCIAS_CANARIAS.includes(provincia) ? 'Atlantic/Canary' : 'Europe/Madrid'
  formatos[zona] ??= new Intl.DateTimeFormat('en-GB', {
    timeZone: zona,
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hourCycle: 'h23',
  })
  const partes = Object.fromEntries(formatos[zona].formatToParts(fecha).map((p) => [p.type, p.value]))
  const dia = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].indexOf(partes.weekday)
  return { dia, minuto: Number(partes.hour) * 60 + Number(partes.minute) }
}

/** Tramos de la semana en minutos desde el lunes a las 0:00. */
function tramos(horario) {
  return horario.flatMap((franjas, dia) =>
    franjas.map((f) => {
      const inicio = dia * MIN_DIA + minutos(f.abre)
      const duracion = (minutos(f.cierra) - minutos(f.abre) + MIN_DIA) % MIN_DIA
      return { dia, inicio, fin: inicio + duracion, abre: f.abre, cierra: f.cierra }
    }),
  )
}

/**
 * ¿Está abierto? Devuelve null si no se conoce el horario, o
 * { abierto, pronto, texto } con un texto como "Abierto hasta las 16:30" o "Cerrado, abre mañana a las 13:00".
 */
export function estadoHorario(est, fecha = new Date()) {
  if (!est?.horario) return null
  const lista = tramos(est.horario)
  if (!lista.length) return { abierto: false, pronto: false, texto: 'Cerrado' }

  const { dia, minuto } = momentoLocal(fecha, est.provincia)
  const ahora = dia * MIN_DIA + minuto
  // Una franja del domingo que acaba el lunes de madrugada también cuenta al principio de la semana.
  const abierta = lista.find((t) => (t.inicio <= ahora && ahora < t.fin) || (t.inicio <= ahora + MIN_SEMANA && ahora + MIN_SEMANA < t.fin))
  if (abierta) {
    const quedan = (abierta.fin - ahora + MIN_SEMANA) % MIN_SEMANA
    const pronto = quedan <= 30
    const texto = pronto ? `Cierra pronto, ${aLas(abierta.cierra)}` : `Abierto ${hasta(abierta.cierra)}`
    return { abierto: true, pronto, texto }
  }

  const espera = (t) => (t.inicio - ahora + MIN_SEMANA) % MIN_SEMANA
  const siguiente = lista.reduce((a, b) => (espera(b) < espera(a) ? b : a))
  const dias = (siguiente.dia - dia + 7) % 7
  const cuando = dias === 0 && siguiente.inicio > ahora ? '' : dias === 1 ? 'mañana ' : dias === 0 ? `el próximo ${DIAS[siguiente.dia].toLowerCase()} ` : `el ${DIAS[siguiente.dia].toLowerCase()} `
  return { abierto: false, pronto: false, texto: `Cerrado, abre ${cuando}${aLas(siguiente.abre)}` }
}

/** "13:00 a 16:30 y 20:30 a 23:30" o "Cerrado". */
export function textoDia(franjas) {
  if (!franjas.length) return 'Cerrado'
  const textos = franjas.map((f) => `${formatoHora(f.abre)} a ${formatoHora(f.cierra)}`)
  return textos.length > 1 ? `${textos.slice(0, -1).join(', ')} y ${textos.at(-1)}` : textos[0]
}

// Un único reloj para toda la app: los estados "abierto/cerrado" se recalculan cada minuto.
const reloj = ref(new Date())
let usuarios = 0
let intervalo

/** Hora actual reactiva (se actualiza cada minuto mientras algún componente la use). */
export function useAhora() {
  if (usuarios++ === 0) intervalo = setInterval(() => (reloj.value = new Date()), 60_000)
  onBeforeUnmount(() => {
    if (--usuarios === 0) clearInterval(intervalo)
  })
  return reloj
}

/** Comprobaciones del formulario (el backend aplica además las suyas). */
export function validarHorario(horario) {
  if (!horario) return ''
  const franjas = horario.flat()
  if (franjas.some((f) => !f.abre || !f.cierra)) return 'Completa las horas de apertura y cierre'
  if (franjas.some((f) => f.abre === f.cierra)) return 'La hora de cierre debe ser distinta de la de apertura'
  return ''
}
