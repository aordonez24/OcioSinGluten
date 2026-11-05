<script setup>
defineProps({
  icono: { type: [Object, Function], required: true },
  titulo: { type: String, required: true },
  texto: { type: String, default: '' },
})
</script>

<template>
  <div class="vacio">
    <component :is="icono" :size="32" :stroke-width="1.5" class="icono" aria-hidden="true" />
    <h3>{{ titulo }}</h3>
    <p v-if="texto" class="suave">{{ texto }}</p>
    <slot />
  </div>
</template>

<style scoped>
.vacio {
  display: grid;
  justify-items: center;
  text-align: center;
  gap: 0.25rem;
  padding: 3rem 1rem;
}

.icono {
  color: var(--turquesa-oscuro);
  margin-bottom: 0.5rem;
}

h3 {
  margin: 0;
  font-size: 1.1rem;
}

p {
  margin: 0 0 0.75rem;
  max-width: 34em;
}
</style>
