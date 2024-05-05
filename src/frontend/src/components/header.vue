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
          <li><router-link :to="{ path: '/establecimientos' }">Establecimientos sin gluten</router-link></li>
          <li><button class="cerrar-sesion-button" @click="scrollToContacto">Contacto</button></li>
          <li><router-link :to="{ path: '/iniciaSesion' }">Inicia Sesión</router-link></li>
          <li><router-link :to="{ path: '/registroUsuario' }">Crea nueva cuenta</router-link></li>
        </ul>
      </nav>
    </header>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

router.beforeEach((to, from, next) => {
  if (to.path === '/establecimientos') {
    // Limpiar la sesión
    sessionStorage.removeItem('token');
  }
  next();
});

const scrollToContacto = () => {
  const elementoContacto = document.getElementById('contacto');
  if (elementoContacto) {
    elementoContacto.scrollIntoView({ behavior: 'smooth' });
  }
}
</script>

<style>
@import "../assets/css/header.css";

.cerrar-sesion-button {
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
}
</style>
