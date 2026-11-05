<script setup>
// Chips con los distintivos sin gluten de un establecimiento.
import { computed } from 'vue'
import { DISTINTIVOS } from '@/catalogo'

const props = defineProps({
  establecimiento: { type: Object, required: true },
  detallado: { type: Boolean, default: false }, // muestra también la explicación
})
const activos = computed(() => DISTINTIVOS.filter((d) => props.establecimiento[d.clave]))
</script>

<template>
  <ul v-if="activos.length && !detallado" class="lista-limpia chips-distintivos">
    <li v-for="d in activos" :key="d.clave" class="chip" :class="{ destacado: d.clave === 'todo_sin_gluten' }" :title="d.descripcion">
      <component :is="d.icono" :size="13" /> {{ d.texto }}
    </li>
  </ul>
  <ul v-else-if="activos.length" class="lista-limpia detallados">
    <li v-for="d in activos" :key="d.clave">
      <span class="icono" :class="{ destacado: d.clave === 'todo_sin_gluten' }"><component :is="d.icono" :size="20" /></span>
      <span>
        <strong>{{ d.texto }}</strong>
        <span class="suave pequeno">{{ d.descripcion }}</span>
      </span>
    </li>
  </ul>
</template>

<style scoped>
.chips-distintivos {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.chip.destacado {
  background: var(--verde-claro);
  color: var(--verde);
}

.detallados {
  display: grid;
  gap: 0.75rem;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
}

.detallados li {
  display: flex;
  gap: 0.65rem;
  align-items: flex-start;
}

.detallados li > span:last-child {
  display: grid;
  line-height: 1.35;
}

.icono {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: var(--radio-sm);
  background: var(--niebla-2);
  color: var(--turquesa-oscuro);
}

.icono.destacado {
  background: var(--verde-claro);
  color: var(--verde);
}
</style>
