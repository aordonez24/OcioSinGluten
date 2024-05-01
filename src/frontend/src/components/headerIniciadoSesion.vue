<script>
export default {
  name: 'header-3',
  data() {
    return {
      username: ''
    };
  },
  mounted() {
    this.username = localStorage.getItem('username');
  },
  methods: {
    async cerrarSesion() {
      try {
        localStorage.removeItem('token');
        this.$router.push('/');
      } catch (error) {
        console.error('Error al cerrar sesión:', error);
      }
    },
    scrollToContacto() {
      const elementoContacto = document.getElementById('contacto');
      if (elementoContacto) {
        elementoContacto.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }
}
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
          <li><router-link to="/iniciadoSesion">Inicio</router-link></li>
          <li><a href="#">Establecimientos sin gluten</a></li>
          <li><button class="cerrar-sesion-button" @click="scrollToContacto">Contacto</button></li>
          <li v-if="username">
            <span class="welcome-message">Bienvenido
              <router-link :to="'/perfil/' + username" class="welcome-link">{{ username }}</router-link>
            </span>
            <button @click="cerrarSesion" class="cerrar-sesion-button">Cerrar sesión</button>
          </li>
        </ul>
      </nav>
    </header>
  </div>
</template>


<style scoped>
@import "../assets/css/header3.css";

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