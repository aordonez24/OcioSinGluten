<script setup>
// Selector de puntuación de 1 a 5 estrellas, accesible con teclado (grupo de radios).
import { computed, ref, useId } from 'vue'
import { Star } from 'lucide-vue-next'
import { TEXTO_PUNTUACION } from '@/catalogo'

const valor = defineModel({ type: Number, default: 0 })
const encima = ref(0)
const nombre = useId()
const mostrado = computed(() => encima.value || valor.value)
</script>

<template>
  <fieldset class="selector" @mouseleave="encima = 0">
    <legend class="solo-lectores">Puntuación</legend>
    <label v-for="n in 5" :key="n" @mouseenter="encima = n">
      <input v-model="valor" type="radio" :name="nombre" :value="n" class="solo-lectores" />
      <Star :size="28" :fill="n <= mostrado ? 'currentColor' : 'none'" :class="{ activa: n <= mostrado }" />
      <span class="solo-lectores">{{ n }} — {{ TEXTO_PUNTUACION[n] }}</span>
    </label>
    <span class="texto" aria-hidden="true">{{ TEXTO_PUNTUACION[mostrado] || 'Elige una puntuación' }}</span>
  </fieldset>
</template>

<style scoped>
.selector {
  display: flex;
  align-items: center;
  gap: 0.1rem;
}

label {
  cursor: pointer;
  color: var(--crema-3);
  display: grid;
  border-radius: 6px;
}

label:has(input:focus-visible) {
  outline: 3px solid rgb(226 96 74 / 45%);
}

.activa {
  color: var(--naranja);
}

svg {
  transition: transform 0.1s;
}

label:hover svg {
  transform: scale(1.12);
}

.texto {
  margin-left: 0.6rem;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--texto-suave);
}
</style>
