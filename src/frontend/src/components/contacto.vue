<template>
  <div id="contacto" class="contactin" v-if="!mensajeEnviado">
    <div class="column">
      <h1>¿Tienes alguna pregunta sobre la celiaquía o los alimentos sin gluten?</h1>
      <p>¡Envíanos un mensaje y estaremos encantados de ayudarte!</p>
    </div>
    <div class="column">
      <form @submit.prevent="handleSubmit">
        <label for="name">Nombre y apellidos:</label>
        <input type="text" id="name" v-model="name" required>
        <label for="email">Correo:</label>
        <input type="email" id="email" v-model="email" required>
        <label for="message">Escribe tu mensaje:</label>
        <textarea id="message" maxlength="140" v-model="message" required></textarea>
        <button type="submit">Enviar</button>
      </form>
    </div>
    <div class="column">
      <p>¡También puedes seguirnos en nuestras redes sociales!</p>
      <div class="social-icons">
        <a href="https://www.instagram.com/ociosingluten/" target="_blank"><i class="fab fa-instagram"></i></a>
        <a href="https://x.com/ociosingluten" target="_blank"><i class="fab fa-twitter"></i></a>
      </div>
    </div>
  </div>
  <div v-else class="contactin mensaje-enviado">
    <h2>¡Mensaje enviado, en breves obtendrás respuestas!</h2>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      name: '',
      email: '',
      message: '',
      mensajeEnviado: false
    };
  },
  methods: {
    handleSubmit() {
      if (!this.validateForm()) {
        return;
      }
      axios.post('http://localhost:8080/ociosingluten/quejas/nuevaQueja', {
        nombre: this.name,
        email: this.email,
        mensaje: this.message
      })
          .then(response => {
            console.log('Mensaje enviado con éxito:', response.data);
            this.mensajeEnviado = true;
          })
          .catch(error => {
            console.error('Error al enviar el mensaje:', error);
          });
    },
    validateForm() {
      if (!this.name || !this.email || !this.message) {
        alert('Todos los campos son obligatorios.');
        return false;
      }
      return true;
    }
  }
};
</script>

<style scoped>
body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
}

.contactin {
  background-color: #353535;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  box-sizing: border-box;
}

.contactin .column {
  width: 100%;
  padding: 20px;
  text-align: center;
  box-sizing: border-box;
}

.contactin h1,
.contactin p {
  font-size: 18px;
  line-height: 1.4;
  max-width: 100%;
  margin: 0 auto 20px;
  color: white;
}

.contactin form {
  text-align: left;
  max-width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

.contactin form label {
  display: block;
  margin-bottom: 8px;
  color: white;
}

.contactin form input,
.contactin form textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 16px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
  color: #333;
  font-size: 16px;
  box-sizing: border-box;
}

.contactin form textarea {
  height: 120px;
}

.contactin form button {
  background-color: #7F7F7F;
  color: white;
  border: none;
  padding: 12px 24px;
  cursor: pointer;
  font-family: 'Montserrat', sans-serif;
  font-size: 16px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
  display: block;
  margin: 0 auto;
}

.contactin form button:hover {
  background-color: #ffcc74;
}

.social-icons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.social-icons a {
  margin: 0 10px;
  color: #fff;
  font-size: 50px;
  transition: color 0.3s ease;
}

.social-icons a:hover {
  color: #ffcc74;
}

.mensaje-enviado {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #353535;
  padding: 40px 20px;
  width: 100%;
  box-sizing: border-box;
}

.mensaje-enviado h2 {
  color: white;
  text-align: center;
}

@media (min-width: 768px) {
  .contactin {
    flex-direction: row;
    justify-content: space-around;
    align-items: flex-start;
  }

  .contactin .column {
    flex: 1;
    max-width: 30%;
    text-align: center;
    margin: 10px;
    box-sizing: border-box;
  }

  .contactin h1,
  .contactin p {
    font-size: 24px;
    max-width: 100%;
  }

  .contactin form {
    max-width: 100%;
  }
}
</style>
