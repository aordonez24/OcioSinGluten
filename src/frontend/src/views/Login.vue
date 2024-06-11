<!-- src/components/Login.vue -->
<template>
  <cabecera-componente2/>
  <div class="container">
    <div class="form-background">
      <h1><span class="black-text">Inicia sesión en</span> <span class="yellow-text">Ocio Sin Gluten</span></h1>
      <form @submit.prevent="submitLogin">
        <input type="text" v-model="email" placeholder="Correo electrónico">
        <br>
        <input type="password" v-model="password" placeholder="Contraseña" @keyup.enter="submitLogin">
        <br><br>
        <button type="submit2">Ingresar</button>
      </form>
      <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
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
import { mapActions } from 'vuex';

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
      errorMessage: ''
    };
  },
  methods: {
    ...mapActions(['login']),
    async submitLogin() {
      try {
        const response = await fetch('http://localhost:8080/ociosingluten/usuarios/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            email: this.email,
            password: this.password
          })
        });

        if (response.ok) {
          this.errorMessage = '';
          const data = await response.json();
          this.login({ token: data.token, username: data.username });
          this.$router.push('/');
        } else if (response.status === 500) {
          this.errorMessage = 'Credenciales inválidas';
        } else {
          this.errorMessage = 'Error: ' + response.statusText;
        }
      } catch (error) {
        this.errorMessage = 'Error: ' + error.message;
      }
    }
  }
};
</script>

<style scoped>
@import "../assets/css/login.css";

.error-message {
  color: #ff0000;
  font-size: 20px;
  margin-top: 5px;
  text-align: center;
}
</style>
