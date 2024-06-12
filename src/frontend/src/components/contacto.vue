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
      console.log("hola");
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

.contactin {
  background-color: #353535; /* Color de fondo gris */
  padding: 80px;
  display: flex;
  justify-content: space-between;
}

.contactin .column {
  flex: 1; /* Las columnas ocupan el mismo espacio */
  padding: 20px; /* Agregar espaciado interno */
  text-align: center; /* Centrar el contenido */
}

.contactin h1,
.contactin p {
  font-size: 30px; /* Tamaño de fuente más pequeño */
  line-height: 1.4; /* Espaciado entre líneas más compacto */
  max-width: 400px; /* Ancho máximo del texto para evitar que se extienda demasiado */
  margin: 0 auto; /* Centrar el texto horizontalmente */
  color: white;
}

.contactin form {
  text-align: left; /* Alinear el contenido del formulario a la izquierda */
  max-width: 400px; /* Limitar el ancho del formulario para que no se vea muy extendido */
  margin: 0 auto; /* Centrar el formulario horizontalmente */
}

.contactin form label {
  display: block; /* Mostrar los labels en una línea nueva */
  margin-bottom: 8px; /* Añadir un poco de espacio entre los labels */
  color: white; /* Color del texto de los labels */
}

.contactin form input,
.contactin form textarea {
  width: calc(100% - 16px); /* Hacer que los campos de entrada y el área de texto ocupen todo el ancho disponible */
  padding: 10px; /* Añadir relleno a los campos de entrada */
  margin-bottom: 16px; /* Añadir espacio entre los campos */
  border: 1px solid #ddd; /* Añadir un borde */
  border-radius: 4px; /* Agregar bordes redondeados */
  background-color: #f9f9f9; /* Color de fondo del campo */
  color: #333; /* Color del texto */
  font-size: 16px; /* Tamaño de fuente */
}

.contactin form textarea {
  height: 120px; /* Establecer una altura para el área de texto */
}

.contactin form button {
  background-color: #7F7F7F; /* Color de fondo del botón */
  color: white; /* Color del texto del botón */
  border: none; /* Eliminar el borde del botón */
  padding: 12px 24px; /* Aumentar el relleno del botón */
  cursor: pointer; /* Cambiar el cursor al pasar sobre el botón */
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */
  font-size: 16px; /* Tamaño de fuente */
  border-radius: 4px; /* Agregar bordes redondeados */
  transition: background-color 0.3s ease; /* Agregar una transición suave al color de fondo */
  display: block; /* Convertir el botón en un elemento de bloque */
  margin: 0 auto; /* Centrar horizontalmente */
}

.contactin form button:hover {
  background-color: #ffcc74; /* Cambiar el color de fondo al pasar el cursor sobre el botón */
}

.social-icons {
  display: flex;
  justify-content: center;
}

.social-icons a {
  margin: 0 20px; /* Ajusta el margen entre los iconos según sea necesario */
  color: #fff; /* Color de los iconos */
  font-size: 36px; /* Tamaño más grande de los iconos */
  transition: color 0.3s ease; /* Agregar una transición suave al color */
}


.social-icons a:hover {
  color: #ffcc74; /* Cambiar el color al pasar el cursor */
}

.mensaje-enviado {
  display: flex;
  align-items: center; /* Centrar verticalmente */
  justify-content: center; /* Centrar horizontalmente */
  background-color: #353535; /* Mantener el fondo gris oscuro */
}

.mensaje-enviado h2 {
  color: white; /* Cambiar el color del texto a blanco */
  text-align: center; /* Asegurarse de que el texto esté centrado */
}
</style>