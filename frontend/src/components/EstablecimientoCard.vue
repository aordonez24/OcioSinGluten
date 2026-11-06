<script setup>
import { computed } from 'vue'
import { Heart, WheatOff } from 'lucide-vue-next'
import { CATEGORIAS, PRECIOS } from '@/catalogo'
import { formatoDistancia, nota } from '@/utils'
import { estadoHorario, useAhora } from '@/horario'
import Estrellas from '@/components/Estrellas.vue'
import Portada from '@/components/Portada.vue'

const props = defineProps({ establecimiento: { type: Object, required: true } })
const e = computed(() => props.establecimiento)
const categoria = computed(() => (CATEGORIAS[e.value.categoria] ?? CATEGORIAS.otro).texto)
const ahora = useAhora()
const estado = computed(() => estadoHorario(e.value, ahora.value))
const lugar = computed(() =>
  e.value.localidad === e.value.provincia ? e.value.localidad : `${e.value.localidad}, ${e.value.provincia}`,
)
</script>

<template>
  <RouterLink :to="`/establecimientos/${e.id}`" class="tarjeta-est">
    <div class="imagen">
      <Portada :url="e.portada_url" :categoria="e.categoria" />
      <span v-if="e.todo_sin_gluten" class="sello"><WheatOff :size="14" aria-hidden="true" /> 100 % sin gluten</span>
    </div>
    <div class="cuerpo">
      <p class="categoria">
        <span>{{ categoria }} en {{ lugar }}</span>
        <span v-if="e.precio" class="precio" :title="PRECIOS[e.precio].texto">
          {{ PRECIOS[e.precio].simbolo }}<span class="solo-lectores">, {{ PRECIOS[e.precio].texto.toLowerCase() }}</span>
        </span>
      </p>
      <h3>{{ e.nombre }}</h3>
      <p v-if="estado" class="estado" :class="{ abierto: estado.abierto }">{{ estado.texto }}</p>
      <p v-if="e.todo_sin_gluten" class="sello-linea"><WheatOff :size="14" aria-hidden="true" /> 100 % sin gluten</p>
      <p class="pie">
        <span class="nota">
          <Estrellas :valor="e.valoracion" :tamano="15" />
          <span v-if="e.num_opiniones" class="suave">
            {{ nota(e.valoracion) }} ({{ e.num_opiniones }})
          </span>
        </span>
        <strong v-if="e.distancia != null" class="distancia">a {{ formatoDistancia(e.distancia) }}</strong>
        <span v-else class="me-gusta suave" :title="`${e.num_likes} me gusta`">
          <Heart :size="14" aria-hidden="true" /> {{ e.num_likes }}
        </span>
      </p>
    </div>
  </RouterLink>
</template>

<style scoped>
.tarjeta-est {
  display: grid;
  align-content: start;
  color: inherit;
  text-decoration: none;
}

.imagen {
  position: relative;
  margin-bottom: 0.65rem;
}

.imagen :deep(.portada) {
  transition: filter 0.15s;
}

.tarjeta-est:hover .imagen :deep(.portada) {
  filter: brightness(0.95);
}

.tarjeta-est:hover h3 {
  text-decoration: underline;
  text-underline-offset: 0.15em;
}

.sello {
  position: absolute;
  left: 0.6rem;
  top: 0.6rem;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.2rem 0.55rem;
  border-radius: var(--radio-sm);
  background: #fff;
  color: var(--verde);
  font-size: 0.8125rem;
  font-weight: 700;
}

.cuerpo {
  display: grid;
  gap: 0.15rem;
  min-width: 0;
}

.sello-linea {
  display: none;
}

.estado {
  margin: 0;
  font-size: 0.875rem;
  color: var(--texto-suave);
}

.estado.abierto {
  color: var(--turquesa-oscuro);
  font-weight: 600;
}

.categoria {
  display: flex;
  justify-content: space-between;
  gap: 0.5rem;
  margin: 0;
  font-size: 0.875rem;
  color: var(--texto-suave);
}

.precio {
  flex-shrink: 0;
  font-weight: 600;
  color: var(--carbon);
  letter-spacing: 0.05em;
}

h3 {
  margin: 0;
  font-size: 1.125rem;
}

.pie {
  margin: 0.2rem 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
}

.nota {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.distancia {
  color: var(--turquesa-oscuro);
  white-space: nowrap;
}

.me-gusta {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

/* En móvil, lista compacta: miniatura a la izquierda y datos a la derecha. */
@media (max-width: 520px) {
  .tarjeta-est {
    grid-template-columns: 104px minmax(0, 1fr);
    gap: 0.9rem;
    align-items: start;
  }

  .imagen {
    margin: 0;
  }

  .tarjeta-est :deep(.portada) {
    aspect-ratio: 1;
    border-radius: var(--radio-sm);
  }

  .tarjeta-est :deep(.sin-foto svg) {
    width: 28px;
    height: 28px;
  }

  .sello {
    display: none;
  }

  .sello-linea {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    margin: 0.1rem 0 0;
    color: var(--verde);
    font-size: 0.8125rem;
    font-weight: 700;
  }

  h3 {
    font-size: 1.0625rem;
  }
}
</style>
