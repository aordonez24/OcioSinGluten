<script setup>
import { computed } from 'vue'
import { Heart, MapPin, WheatOff } from 'lucide-vue-next'
import { CATEGORIAS } from '@/catalogo'
import { formatoDistancia } from '@/utils'
import Estrellas from '@/components/Estrellas.vue'
import Portada from '@/components/Portada.vue'

const props = defineProps({ establecimiento: { type: Object, required: true } })
const categoria = computed(() => CATEGORIAS[props.establecimiento.categoria] ?? CATEGORIAS.otro)
</script>

<template>
  <RouterLink :to="`/establecimientos/${establecimiento.id}`" class="tarjeta-est">
    <div class="imagen">
      <Portada :url="establecimiento.portada_url" :nombre="establecimiento.nombre" />
      <span v-if="establecimiento.todo_sin_gluten" class="sello"><WheatOff :size="14" /> 100 % sin gluten</span>
    </div>
    <div class="cuerpo">
      <p class="categoria"><component :is="categoria.icono" :size="14" /> {{ categoria.texto }}</p>
      <h3>{{ establecimiento.nombre }}</h3>
      <p class="lugar suave">
        <MapPin :size="15" /> {{ establecimiento.localidad }}
        <span v-if="establecimiento.localidad !== establecimiento.provincia">· {{ establecimiento.provincia }}</span>
        <strong v-if="establecimiento.distancia != null" class="distancia">a {{ formatoDistancia(establecimiento.distancia) }}</strong>
      </p>
      <div class="pie">
        <span class="nota">
          <Estrellas :valor="establecimiento.valoracion" />
          <span v-if="establecimiento.num_opiniones" class="suave pequeno">
            <strong>{{ establecimiento.valoracion }}</strong> ({{ establecimiento.num_opiniones }})
          </span>
        </span>
        <span class="suave pequeno me-gusta" title="Me gusta"><Heart :size="14" /> {{ establecimiento.num_likes }}</span>
      </div>
    </div>
  </RouterLink>
</template>

<style scoped>
.tarjeta-est {
  display: flex;
  flex-direction: column;
  background: var(--blanco);
  border: 1px solid var(--borde);
  border-radius: var(--radio);
  overflow: hidden;
  color: inherit;
  text-decoration: none;
  box-shadow: var(--sombra);
  transition: transform 0.15s, box-shadow 0.15s;
}

.tarjeta-est:hover {
  transform: translateY(-3px);
  box-shadow: var(--sombra-alta);
}

.imagen {
  position: relative;
}

.sello {
  position: absolute;
  left: 0.6rem;
  top: 0.6rem;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: #fff;
  color: var(--verde);
  font-size: 0.75rem;
  font-weight: 700;
  box-shadow: var(--sombra);
}

@media (max-width: 520px) {
  .tarjeta-est :deep(.portada) {
    aspect-ratio: 5 / 2;
  }
}

.cuerpo {
  padding: 0.85rem 1.1rem 1rem;
  display: grid;
  gap: 0.25rem;
}

.categoria {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--coral-oscuro);
}

h3 {
  margin: 0;
  font-size: 1.08rem;
}

.lugar {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.88rem;
}

.distancia {
  margin-left: auto;
  color: var(--coral-oscuro);
  font-size: 0.82rem;
  white-space: nowrap;
}

.pie {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.35rem;
}

.nota {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.nota strong {
  color: var(--texto);
}

.me-gusta {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}
</style>
