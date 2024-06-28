<!-- src/components/header-3.vue -->
<script>
import { mapGetters } from 'vuex';
import axios from "axios";

export default {
  name: 'header-3',
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  data(){
    return{
      rol: '',
    }
  },
  mounted() {
    this.obtenerUsuario();
  },
  methods: {
    async obtenerUsuario() {
      try {
        this.loading = true;
        const username = this.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${username}`);
        this.rol = response.data.rol;
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    },
    scrollToContacto() {
      const elementoContacto = document.getElementById('contacto');
      if (elementoContacto) {
        elementoContacto.scrollIntoView({ behavior: 'smooth' });
      }
    },
    gotoQuejas() {
      this.$router.push('/quejas');
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
          <h2 class="subtitle">Disfrute de su tiempo de ocio sin gluten</h2>
        </div>
      </div>
      <nav>
        <ul class="main-links">
          <li><router-link to="/" class="cerrar-sesion-button">Inicio</router-link></li>
          <li><router-link to="/establecimientos" class="cerrar-sesion-button">Establecimientos sin gluten</router-link></li>
          <li><router-link to="/actividades" class="cerrar-sesion-button">Actividades</router-link></li>
          <li><router-link to="/comunidad" class="cerrar-sesion-button">Comunidad</router-link></li>
          <li><button class="cerrar-sesion-button" @click="scrollToContacto">Contacto</button></li>
          <li><button v-if="rol === 'ADMIN'" class="cerrar-sesion-button" @click="gotoQuejas">Consultas</button></li>
          <li v-if="isAuthenticated">
            <span class="welcome-message">Bienvenido
              <router-link :to="'/perfil/' + username" class="welcome-link">{{ username }}</router-link>
            </span>
          </li>
        </ul>
      </nav>
    </header>
  </div>
</template>

<style scoped>

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

.welcome-message {
  font-size: 26px;
  font-weight: bold;
  color: #ffa500;
  margin-right: 10px;
  display: inline;
}

.welcome-link {
  text-decoration: none;
  width: 100%;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #666666;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  display: inline; /* Hacer que el elemento sea en línea */
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

/* Media Queries para diseño responsive */
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
