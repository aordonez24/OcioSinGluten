// Categorías y distintivos de los establecimientos (los mismos valores que el backend).
import { BadgeCheck, ChefHat, Coffee, Croissant, IceCreamCone, ScrollText, Store, UtensilsCrossed, WheatOff, Wine } from 'lucide-vue-next'

export const CATEGORIAS = {
  restaurante: { texto: 'Restaurante', icono: UtensilsCrossed },
  cafeteria: { texto: 'Cafetería', icono: Coffee },
  panaderia: { texto: 'Panadería y pastelería', icono: Croissant },
  bar: { texto: 'Bar', icono: Wine },
  heladeria: { texto: 'Heladería', icono: IceCreamCone },
  otro: { texto: 'Otro', icono: Store },
}

export const DISTINTIVOS = [
  {
    clave: 'todo_sin_gluten',
    texto: '100 % sin gluten',
    descripcion: 'Todo lo que sirven es apto: no entra gluten en el local.',
    icono: WheatOff,
  },
  {
    clave: 'carta_sin_gluten',
    texto: 'Carta sin gluten',
    descripcion: 'Tienen una carta o platos marcados como aptos.',
    icono: ScrollText,
  },
  {
    clave: 'cocina_separada',
    texto: 'Cocina separada',
    descripcion: 'Preparan lo sin gluten aparte para evitar la contaminación cruzada.',
    icono: ChefHat,
  },
  {
    clave: 'acreditado',
    texto: 'Acreditado',
    descripcion: 'Cuenta con la acreditación de una asociación de celíacos.',
    icono: BadgeCheck,
  },
]

export const TEXTO_PUNTUACION = ['', 'Muy mal', 'Regular', 'Bien', 'Muy bien', 'Excelente']

export const PRECIOS = {
  1: { simbolo: '€', texto: 'Económico' },
  2: { simbolo: '€€', texto: 'Precio medio' },
  3: { simbolo: '€€€', texto: 'Caro' },
}

// Motivos para avisar al equipo de administración de un problema con un sitio:
// `opcion` es lo que lee quien avisa y `resumen`, lo que ve el equipo en su bandeja.
export const MOTIVOS_AVISO = {
  cerrado: { opcion: 'Ha cerrado', resumen: 'Ha cerrado' },
  sin_opciones: { opcion: 'Ya no tiene opciones sin gluten', resumen: 'Ya no tiene opciones sin gluten' },
  datos_incorrectos: { opcion: 'Algún dato está mal (dirección, teléfono, horario…)', resumen: 'Datos incorrectos' },
  otro: { opcion: 'Otro motivo', resumen: 'Otro problema' },
}
