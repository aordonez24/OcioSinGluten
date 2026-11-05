<script setup>
import logo from '@/assets/logo.webp'
</script>

<template>
  <footer>
    <div class="interior">
      <div class="marca">
        <img :src="logo" alt="" width="36" height="36" />
        <div>
          <strong>Ocio Sin Gluten</strong>
          <p>La comunidad para comer fuera sin preocupaciones.</p>
        </div>
      </div>
      <nav aria-label="Pie de página">
        <RouterLink to="/establecimientos">Explorar</RouterLink>
        <RouterLink to="/actividad">Actividad</RouterLink>
        <RouterLink :to="{ path: '/', hash: '#contacto' }">Contacto</RouterLink>
      </nav>
      <p class="credito">
        Trabajo Fin de Grado · Álvaro Ordóñez Romero · Universidad de Jaén
      </p>
    </div>
  </footer>
</template>

<style scoped>
footer {
  background: var(--marron);
  color: var(--crema-2);
  font-size: 0.9rem;
  margin-top: 2rem;
}

.interior {
  width: min(1120px, 100% - 2rem);
  margin-inline: auto;
  padding: 1.75rem 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem 2rem;
}

.marca {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.marca img {
  border-radius: 50%;
}

.marca p {
  margin: 0;
  opacity: 0.75;
}

nav {
  display: flex;
  gap: 1.25rem;
}

a {
  color: var(--crema);
  text-decoration: none;
  font-weight: 600;
}

a:hover {
  color: var(--naranja);
}

.credito {
  width: 100%;
  margin: 0;
  padding-top: 1rem;
  border-top: 1px solid rgb(255 255 255 / 10%);
  opacity: 0.6;
  font-size: 0.82rem;
}
</style>
