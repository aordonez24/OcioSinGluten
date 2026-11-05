<script setup>
import { Star } from 'lucide-vue-next'
import { computed } from 'vue'

const props = defineProps({ valor: { type: Number, default: null }, tamano: { type: Number, default: 16 } })

// Porcentaje de relleno de cada una de las 5 estrellas (permite medias estrellas exactas).
const rellenos = computed(() => [0, 1, 2, 3, 4].map((i) => Math.max(0, Math.min(1, props.valor - i)) * 100))
</script>

<template>
  <span v-if="valor !== null" class="estrellas" :title="`${valor} de 5`" role="img" :aria-label="`${valor} de 5 estrellas`">
    <span v-for="(r, i) in rellenos" :key="i" class="estrella">
      <Star :size="tamano" class="vacia" />
      <span class="llena" :style="{ width: `${r}%` }"><Star :size="tamano" fill="currentColor" /></span>
    </span>
  </span>
  <span v-else class="suave sin-valorar">Sin valoraciones</span>
</template>

<style scoped>
.estrellas {
  display: inline-flex;
  gap: 1px;
  color: var(--estrella);
}

.estrella {
  position: relative;
  display: inline-flex;
}

.vacia {
  opacity: 0.45;
}

.llena {
  position: absolute;
  inset: 0 auto 0 0;
  overflow: hidden;
  display: inline-flex;
}

.llena svg {
  flex-shrink: 0;
}

.sin-valorar {
  font-size: 0.85rem;
}
</style>
