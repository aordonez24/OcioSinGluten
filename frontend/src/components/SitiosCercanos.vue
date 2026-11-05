<script setup>
// "Más sitios sin gluten cerca": los establecimientos más próximos a uno dado (hasta 60 km).
// Si el sitio no tiene coordenadas, se usan los de su misma provincia.
import { computed, onMounted, ref } from 'vue'
import { api } from '@/api'
import { distanciaKm } from '@/utils'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'

const props = defineProps({ establecimiento: { type: Object, required: true } })
const MAXIMO = 4
const RADIO_KM = 60

const todos = ref([])
const origen = computed(() => props.establecimiento)

const cercanos = computed(() => {
  const otros = todos.value.filter((e) => e.id !== origen.value.id)
  if (origen.value.latitud == null) {
    return otros.filter((e) => e.provincia === origen.value.provincia).slice(0, MAXIMO)
  }
  return otros
    .filter((e) => e.latitud != null)
    .map((e) => ({ ...e, distancia: distanciaKm(origen.value, e) }))
    .filter((e) => e.distancia <= RADIO_KM)
    .sort((a, b) => a.distancia - b.distancia)
    .slice(0, MAXIMO)
})

onMounted(async () => {
  todos.value = await api('/establecimientos').catch(() => [])
})
</script>

<template>
  <section v-if="cercanos.length" class="cercanos" aria-labelledby="titulo-cercanos">
    <h2 id="titulo-cercanos">Más sitios sin gluten cerca</h2>
    <div class="rejilla">
      <EstablecimientoCard v-for="e in cercanos" :key="e.id" :establecimiento="e" />
    </div>
  </section>
</template>

<style scoped>
.cercanos {
  padding-top: 2.5rem;
  border-top: 1px solid var(--borde);
}
</style>
