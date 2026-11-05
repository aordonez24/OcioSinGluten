<script setup>
import { Heart, MapPin, Footprints } from 'lucide-vue-next'
import Estrellas from '@/components/Estrellas.vue'
import Portada from '@/components/Portada.vue'

defineProps({ establecimiento: { type: Object, required: true } })
</script>

<template>
  <RouterLink :to="`/establecimientos/${establecimiento.id}`" class="tarjeta-est">
    <Portada :url="establecimiento.portada_url" :nombre="establecimiento.nombre" />
    <div class="cuerpo">
      <h3>{{ establecimiento.nombre }}</h3>
      <p class="lugar suave">
        <MapPin :size="15" /> {{ establecimiento.localidad }}
        <span v-if="establecimiento.localidad !== establecimiento.provincia">· {{ establecimiento.provincia }}</span>
      </p>
      <div class="pie">
        <Estrellas :valor="establecimiento.valoracion" />
        <span class="contadores suave">
          <span title="Me gusta"><Heart :size="14" /> {{ establecimiento.num_likes }}</span>
          <span title="Visitas"><Footprints :size="14" /> {{ establecimiento.num_visitas }}</span>
        </span>
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

@media (max-width: 520px) {
  .tarjeta-est :deep(.portada) {
    aspect-ratio: 5 / 2;
  }
}

.cuerpo {
  padding: 0.9rem 1.1rem 1rem;
  display: grid;
  gap: 0.3rem;
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

.pie {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.35rem;
}

.contadores {
  display: flex;
  gap: 0.75rem;
  font-size: 0.85rem;
}

.contadores span {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}
</style>
