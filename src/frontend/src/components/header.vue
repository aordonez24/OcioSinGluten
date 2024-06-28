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
header {
  background-color: #ffffff;
  padding: 5px;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  margin-right: 20px;
}

.title-container {
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 50px;
  margin-bottom: 5px;
}

.subtitle {
  font-size: 20px;
  margin-top: 0;
}

nav {
  margin-left: 30px;
}

nav ul.main-links {
  list-style-type: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  margin-left: 0;
}

nav ul.main-links li {
  margin-right: 10px;
}

nav ul.main-links li:last-child {
  margin-right: 0;
}

nav ul li a {
  text-decoration: none;
  display: block;
  width: 100%;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

nav ul li a:hover {
  background-color: #9DD9D2;
  border-color: transparent;
}

nav ul li:nth-child(4) a {
  background-color: #9DD9D2;
  color: white;
  border-color: transparent;
}

nav ul li:nth-child(5) a {
  background-color: #7F7F7F;
  color: white;
  border-color: transparent;
}

nav ul li:nth-child(4) a:hover {
  background-color: #ffa500;
  border-color: transparent;
}


nav ul li:nth-child(5) a:hover {
  background-color: #ffa500;
  border-color: transparent;
}

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

@media (max-width: 992px) {
  .logo-container {
    flex-direction: column;
  }

  .logo {
    width: 100px;
    height: 100px;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .title {
    font-size: 2em;
  }

  .subtitle {
    font-size: 1em;
  }

  nav {
    margin-left: 0;
    margin-top: 20px;
  }

  nav ul.main-links {
    flex-direction: column;
    align-items: center;
  }

  nav ul.main-links li {
    margin-right: 0;
    margin-bottom: 10px;
  }

  nav ul.main-links li:last-child {
    margin-bottom: 0;
  }

  .welcome-message {
    font-size: 1.25em;
  }

  .welcome-link {
    font-size: 1em;
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 1.5em;
  }

  .subtitle {
    font-size: 0.875em;
  }

  .welcome-message {
    font-size: 1em;
  }

  .welcome-link {
    font-size: 0.875em;
  }
}

@media (max-width: 576px) {
  .title {
    font-size: 1.25em;
  }

  .subtitle {
    font-size: 0.75em;
  }

  .welcome-message {
    font-size: 0.875em;
  }

  .welcome-link {
    font-size: 0.75em;
  }
}
</style>
