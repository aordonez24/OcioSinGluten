<script setup>
// Foto del establecimiento o, si aún no tiene, un fondo plano con el icono de su tipo.
// Nada de imágenes inventadas: la portada mejora sola en cuanto alguien sube una foto.
import { computed } from 'vue'
import { CATEGORIAS } from '@/catalogo'
import { recurso } from '@/plataforma'

const props = defineProps({
  url: { type: String, default: null },
  categoria: { type: String, default: 'otro' },
})
const icono = computed(() => (CATEGORIAS[props.categoria] ?? CATEGORIAS.otro).icono)
</script>

<template>
  <div class="portada">
    <img v-if="url" :src="recurso(url)" alt="" loading="lazy" />
    <div v-else class="sin-foto" aria-hidden="true">
      <component :is="icono" :size="36" :stroke-width="1.5" />
    </div>
  </div>
</template>

<style scoped>
.portada {
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: var(--radio);
  background: var(--niebla);
}

.portada img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sin-foto {
  height: 100%;
  display: grid;
  place-items: center;
  color: #8fb3ae;
}
</style>
