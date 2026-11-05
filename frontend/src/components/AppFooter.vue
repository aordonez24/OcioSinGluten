<script setup>
import logo from '@/assets/logo.webp'
</script>

<template>
  <footer>
    <div class="interior">
      <div class="marca">
        <img :src="logo" alt="" width="44" height="44" />
        <p>
          <strong>Ocio Sin Gluten</strong>
          <span>Restaurantes y sitios de ocio sin gluten recomendados por personas celíacas.</span>
        </p>
      </div>
      <nav aria-label="Pie de página">
        <RouterLink to="/establecimientos">Establecimientos</RouterLink>
        <RouterLink to="/actividad">Actividades</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#contacto' }">Contacto</RouterLink>
      </nav>
      <p class="credito">Trabajo Fin de Grado de Álvaro Ordóñez Romero en la Universidad de Jaén.</p>
    </div>
  </footer>
</template>

<style scoped>
/* El pie turquesa del proyecto original. */
footer {
  background: var(--turquesa);
  color: var(--carbon);
  font-size: 0.9375rem;
}

.interior {
  width: min(var(--ancho), 100% - 2 * var(--margen));
  margin-inline: auto;
  padding: 2rem 0 1.5rem;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1.25rem 2rem;
}

.marca {
  display: flex;
  align-items: center;
  gap: 0.85rem;
}

.marca img {
  border-radius: 50%;
}

.marca p {
  margin: 0;
  display: grid;
  line-height: 1.4;
}

nav {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 1.5rem;
}

a {
  color: var(--carbon);
  font-weight: 600;
  text-decoration: none;
  min-height: 44px;
  display: inline-flex;
  align-items: center;
}

a:hover {
  text-decoration: underline;
  color: var(--carbon);
}

.credito {
  width: 100%;
  margin: 0;
  padding-top: 1.25rem;
  border-top: 1px solid rgb(53 53 53 / 18%);
  font-size: 0.875rem;
}
</style>
