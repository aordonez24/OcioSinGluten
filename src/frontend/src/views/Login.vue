<template>
  <cabecera-componente2/>
  <div class="container">
    <div class="form-background">
      <h1><span class="black-text">Inicia sesión en</span> <span class="yellow-text">Ocio Sin Gluten</span></h1>
      <form @submit.prevent="login">
        <input type="text" v-model="email" placeholder="Correo electrónico">
        <br>
        <input type="password" v-model="password" placeholder="Contraseña">
        <br><br>
        <button type="submit2">Ingresar</button>
      </form>
    </div>
    <div class="login">
      <p>¿Necesitas una cuenta? <a> <router-link :to="{ path: '/registroUsuario' }">Registrarse</router-link> </a></p>
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
            'Content-Type': 'application/x-www-form-urlencoded'
          },
          body: new URLSearchParams({
            email: this.email,
            password: this.password
          })
        });

        if (response.ok) {
          // Si la respuesta del servidor es exitosa (código 2xx), mostrar un mensaje de éxito o redireccionar al usuario
          this.errorMessage = ''; // Limpiar el mensaje de error si no hay error
          const data = await response.json();
          console.log(data); // Manejar la respuesta del servidor
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
