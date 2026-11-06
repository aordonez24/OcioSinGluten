<script setup>
// "Desliza hacia abajo para actualizar", como en las apps del móvil. Solo con el dedo y estando
// arriba del todo de la página; el mapa (Leaflet) se queda con sus propios gestos. Con ratón o
// teclado no hace nada: los datos se actualizan al volver a entrar en la página.
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { RefreshCw } from 'lucide-vue-next'
import { vibrar } from '@/plataforma'

const props = defineProps({ alRecargar: { type: Function, required: true } })

const UMBRAL = 70 // px que hay que estirar para recargar
const MAXIMO = 110
const distancia = ref(0)
const recargando = ref(false)
let inicioY = null
let avisado = false

function empezar(e) {
  if (recargando.value || window.scrollY > 0 || e.touches.length > 1) return
  if (e.target.closest('.leaflet-container, dialog, .visor, input, textarea, select')) return
  inicioY = e.touches[0].clientY
  avisado = false
}

function mover(e) {
  if (inicioY === null) return
  const tirado = e.touches[0].clientY - inicioY
  if (tirado <= 0 || window.scrollY > 0) {
    distancia.value = 0
    return
  }
  distancia.value = Math.min(MAXIMO, tirado * 0.5) // resistencia, como una goma
  if (distancia.value >= UMBRAL && !avisado) {
    avisado = true
    vibrar() // se nota en el dedo el punto en que ya recarga
  }
}

async function soltar() {
  if (inicioY === null) return
  inicioY = null
  if (distancia.value < UMBRAL) {
    distancia.value = 0
    return
  }
  recargando.value = true
  distancia.value = UMBRAL
  try {
    await Promise.all([props.alRecargar(), new Promise((r) => setTimeout(r, 400))]) // que se vea girar
  } finally {
    recargando.value = false
    distancia.value = 0
  }
}

onMounted(() => {
  // El navegador tiene su propio gesto (recarga la página entera y estira el contenido): aquí lo
  // sustituye este, que solo vuelve a pedir los datos.
  document.documentElement.style.overscrollBehaviorY = 'none'
  window.addEventListener('touchstart', empezar, { passive: true })
  window.addEventListener('touchmove', mover, { passive: true })
  window.addEventListener('touchend', soltar)
  window.addEventListener('touchcancel', soltar)
})
onBeforeUnmount(() => {
  document.documentElement.style.overscrollBehaviorY = ''
  window.removeEventListener('touchstart', empezar)
  window.removeEventListener('touchmove', mover)
  window.removeEventListener('touchend', soltar)
  window.removeEventListener('touchcancel', soltar)
})
</script>

<template>
  <div
    class="recarga"
    :class="{ activa: distancia > 0, recargando, lista: distancia >= UMBRAL }"
    :style="{ '--tirado': `${distancia}px`, '--giro': `${distancia * 3}deg` }"
    aria-hidden="true"
  >
    <span class="circulo"><RefreshCw :size="20" /></span>
  </div>
  <p class="solo-lectores" role="status">{{ recargando ? 'Actualizando…' : '' }}</p>
</template>

<style scoped>
.recarga {
  position: fixed;
  top: calc(var(--alto-cabecera) + env(safe-area-inset-top));
  left: 50%;
  z-index: 90;
  translate: -50% calc(var(--tirado) - 48px);
  opacity: 0;
  pointer-events: none;
  transition:
    translate 0.25s ease,
    opacity 0.2s ease;
}

.recarga.activa {
  opacity: 1;
  transition: none; /* sigue al dedo sin retraso */
}

.recarga.recargando {
  opacity: 1;
  transition: translate 0.25s ease;
}

.circulo {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--blanco);
  color: var(--texto-suave);
  box-shadow: 0 2px 8px rgb(0 0 0 / 18%);
  rotate: var(--giro);
}

.lista .circulo,
.recargando .circulo {
  color: var(--turquesa-oscuro);
}

.recargando .circulo {
  animation: girar 0.8s linear infinite;
}

@keyframes girar {
  to {
    rotate: 360deg;
  }
}

@media (prefers-reduced-motion: reduce) {
  .recargando .circulo {
    animation-duration: 2.4s;
  }
}
</style>
