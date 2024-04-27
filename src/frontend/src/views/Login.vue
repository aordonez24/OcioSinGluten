<template>
  <cabecera-componente2/>
  <div class="container">
    <div class="form-background">
      <h1><span class="black-text">Inicia sesión en</span> <span class="yellow-text">Ocio Sin Gluten</span></h1>
      <form @submit.prevent="login">
        <input type="text" v-model="email" placeholder="Correo electrónico">
        <br>
        <input type="password" v-model="password" placeholder="Contraseña" @keyup.enter="login">
        <br><br>
        <button type="submit2">Ingresar</button>
      </form>
    </div>
    <div class="login">
      <p>¿Necesitas una cuenta? <a> <router-link :to="{ path: '/registroUsuario' }">Crea una nueva</router-link> </a></p>
    </div>
  </div>
  <div class="extra-space">
    <footer-componente/>
  </div>
</template>

<script>
import CabeceraComponente2 from "@/components/header2.vue";
import FooterComponente from "@/components/footer.vue";

export default {

  name: 'Vista-Login',
  components: {
    FooterComponente,
    CabeceraComponente2
  },
  data() {
    return {
      email: '',
      password: '',
      errorMessage: '' // Variable para almacenar el mensaje de error
    };
  },
  methods: {
    async login() {
      try {
        const response = await fetch('http://localhost:8080/ociosingluten/usuarios/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json' // Cambiar el tipo de contenido a application/json
          },
          body: JSON.stringify({
            email: this.email,
            password: this.password
          })
        });

        if (response.ok) {
          // Si la respuesta del servidor es exitosa (código 2xx), mostrar un mensaje de éxito o redireccionar al usuario
          this.errorMessage = ''; // Limpiar el mensaje de error si no hay error
          const data = await response.json();
          console.log(data); // Manejar la respuesta del servidor

          const token = data.token; // Obtener el token de la respuesta
          const username = data.username; // Obtener el nombre de usuario de la respuesta

          // Almacenar el token de autenticación y el nombre de usuario en localStorage
          localStorage.setItem('token', token);
          localStorage.setItem('username', username);

          this.$router.push('/iniciadoSesion'); // Reemplaza '/otra-vista' con la ruta de la vista deseada
        } else if (response.status === 500) {
          // Si la respuesta del servidor es un error 500, mostrar un mensaje de error de credenciales inválidas
          this.errorMessage = 'Credenciales inválidas';
        } else {
          // En caso de cualquier otro error, mostrar un mensaje genérico de error
          this.errorMessage = 'Error: ' + response.statusText;
        }
      } catch (error) {
        // Si hay un error en la solicitud, mostrar un mensaje genérico de error
        this.errorMessage = 'Error: ' + error.message;
      }
    }
  }
};
</script>

<style scoped>
@import "../assets/css/login.css";

</style>
