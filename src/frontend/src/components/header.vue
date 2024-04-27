<script setup>

</script>

<template>
  <div>
    <header>
      <div class="logo-container">
        <img src="../assets/images/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg" alt="Logo de la aplicación" class="logo">
        <div class="title-container">
          <h1 class="title">Ocio Sin Gluten</h1>
          <h2 class="subtitle">Disfrute de su tiempo de ocio sin gluten</h2> <!-- Subtítulo debajo del título -->
        </div>
      </div>
      <nav>
        <ul class="main-links">
          <li><router-link to="/">Inicio</router-link></li>
          <li><a href="#">Establecimientos sin gluten</a></li>
          <li><router-link :to="{ path: '/contacto' }">Contacto</router-link></li>
          <li><router-link :to="{ path: '/iniciaSesion' }">Inicia Sesión</router-link></li>
          <li><router-link :to="{ path: '/registroUsuario' }">Crea nueva cuenta</router-link></li>
        </ul>
      </nav>
    </header>
  </div>
</template>

<script>
export default {
  name: 'cabecera-componente'
}

</script>

<style>
@import "../assets/css/header.css";
</style>