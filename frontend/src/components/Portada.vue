<script setup>
// Imagen del establecimiento o, si no tiene fotos, una portada ilustrada con su inicial
// en un color estable derivado del nombre.
import { computed } from 'vue'
import { UtensilsCrossed } from 'lucide-vue-next'

const props = defineProps({
  url: { type: String, default: null },
  nombre: { type: String, required: true },
})

const PALETAS = [
  ['#e2604a', '#f2a07a'],
  ['#e9a15a', '#f5cf8e'],
  ['#4a3121', '#8a5a3c'],
  ['#2f7d4f', '#7cbf8e'],
  ['#c24a35', '#e9a15a'],
  ['#6b4f9e', '#b39ad8'],
]
const fondo = computed(() => {
  const hash = [...props.nombre].reduce((h, c) => (h * 31 + c.charCodeAt(0)) >>> 0, 7)
  const [a, b] = PALETAS[hash % PALETAS.length]
  return `linear-gradient(135deg, ${a}, ${b})`
})
</script>

<template>
  <div class="portada">
    <img v-if="url" :src="url" alt="" loading="lazy" />
    <div v-else class="ilustrada" :style="{ background: fondo }" aria-hidden="true">
      <span class="inicial">{{ nombre.slice(0, 1).toUpperCase() }}</span>
      <UtensilsCrossed class="icono" :size="22" />
    </div>
  </div>
</template>

<style scoped>
.portada {
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: var(--crema-2);
}

.portada img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ilustrada {
  position: relative;
  height: 100%;
  display: grid;
  place-items: center;
  color: rgb(255 255 255 / 92%);
}

.inicial {
  font-size: clamp(2.5rem, 7vw, 3.5rem);
  font-weight: 800;
  text-shadow: 0 2px 12px rgb(0 0 0 / 15%);
}

.icono {
  position: absolute;
  right: 0.85rem;
  bottom: 0.75rem;
  opacity: 0.7;
}
</style>
