<!-- src/components/Login.vue -->
<template>
  <cabecera-componente2/>
  <div class="container1">
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

@import url('https://fonts.googleapis.com/css?family=Montserrat');
@import url('https://fonts.googleapis.com/css?family=Lato');


* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
}

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat;
  background-size: cover;
  padding: 20px;
}


.container {
  width: 510px;
  margin: 50px auto 100px;
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
}


h1 {
  font-family: 'Montserrat', sans-serif;
  color: black;
  text-align: center;
  font-size: 1.5rem;
  margin-bottom: 32px;
}

/* FORM */

.form-background {
  background: #fff;
  width: 420px;
  border-radius: 10px;
  padding: 32px;
  box-shadow: 0px 15px 88px -37px rgba(0, 0, 0, 0.17);
}

.yellow-text {
  font-size: 40px;
  color: #ffcc74;
}

.black-text {
  font-size: 40px;
  color: black;
}


input[type="text"] {
  width: 100%;
  margin-bottom: 10px;
  border-radius: 30px;
  border: 1px solid #DAE2F1;
  padding: 15px 20px;
  color: #8498af;
  font-size: 0.9rem;
  transition: 0.4s;
}


form {
  text-transform: capitalize;
  font-family: 'Lato', sans-serif;
  text-align: center;
}

input {
  width: 100%;
  margin-bottom: 20px;
  border-radius: 30px;
  border: 1px solid #DAE2F1;
  padding: 15px 20px;
  color: #8498af;
  font-size: 0.9rem;
  transition: 0.4s;
}

[type="submit2"] {
  background: #7F7F7F;
  border-radius: 30px;
  padding: 16px 30px;
  width: 100%;
  color: #fff;
  text-transform: uppercase;
  font-size: 0.8rem;
  transition: 0.4s;
  font-family: 'Montserrat', sans-serif;
  border: none;
  box-shadow: none;
}

[type="submit2"]:hover {
  color: #ffcc74;
}

/* LOGIN */

.login {
  text-align: center;
  margin-top: 24px;
  font-size: 0.8rem;
  font-family: 'Montserrat', sans-serif;
}

.login a {
  font-size: 20px;
  margin-left: 4px;
  text-decoration: none;
  color: #7F7F7F;
  transition: 0.3s;
}

.login p {
  font-size: 20px;
  margin-left: 4px;
  text-decoration: none;
  color: #7F7F7F;
  transition: 0.3s;
}

.login a:hover {
  color: #ffcc74;
}

input[type="password"] {
  padding-right: 30px;
}

.fa-eye {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
}

.error-message {
  color: #ff0000;
  font-size: 20px;
  margin-top: 5px;
  text-align: center;
}

@media screen and (max-width: 768px) {
  .container {
    width: 90%;
    margin: 50px auto;
    padding: 20px;
  }

  .form-background {
    width: 100%;
    padding: 20px;
  }

  h1 {
    font-size: 24px;
  }

  input[type="text"],
  input[type="password"] {
    width: 100%;
  }

  button[type="submit"] {
    width: 100%;
  }

  .login {
    font-size: 16px;
  }
}

</style>
