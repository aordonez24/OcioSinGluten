<script setup>
import { computed } from 'vue'
import { recurso } from '@/plataforma'

const props = defineProps({
  usuario: { type: Object, required: true },
  tamano: { type: Number, default: 40 },
})

const iniciales = computed(() => (props.usuario.nombre || props.usuario.username || '?').slice(0, 1).toUpperCase())
</script>

<template>
  <img
    v-if="usuario.foto_url"
    :src="recurso(usuario.foto_url)"
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
    role="img"
    :aria-label="`Foto de ${usuario.username}`"
  >
    {{ iniciales }}
  </span>
</template>

<style scoped>
.inicial {
  display: inline-grid;
  place-items: center;
  background: var(--turquesa);
  color: var(--carbon);
  font-weight: 650;
}
</style>
