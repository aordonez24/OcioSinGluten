<script setup>
import { computed } from 'vue'

const props = defineProps({
  usuario: { type: Object, required: true },
  tamano: { type: Number, default: 40 },
})

const iniciales = computed(() => (props.usuario.nombre || props.usuario.username || '?').slice(0, 1).toUpperCase())
</script>

<template>
  <img
    v-if="usuario.foto_url"
    :src="usuario.foto_url"
    :alt="`Foto de ${usuario.username}`"
    class="avatar"
    :width="tamano"
    :height="tamano"
    :style="{ width: `${tamano}px`, height: `${tamano}px` }"
    loading="lazy"
  />
  <span
    v-else
    class="avatar inicial"
    :style="{ width: `${tamano}px`, height: `${tamano}px`, fontSize: `${tamano * 0.45}px` }"
    aria-hidden="true"
  >
    {{ iniciales }}
  </span>
</template>

<style scoped>
.inicial {
  display: inline-grid;
  place-items: center;
  background: var(--naranja);
  color: var(--marron);
  font-weight: 700;
}
</style>
