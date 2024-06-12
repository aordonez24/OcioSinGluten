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
  overflow-x: hidden; /* Prevenir el desbordamiento horizontal */
}

.contactin {
  background-color: #353535; /* Color de fondo gris */
  padding: 20px; /* Reducir el padding para pantallas pequeñas */
  display: flex;
  flex-direction: column; /* Apilar las columnas verticalmente en pantallas pequeñas */
  align-items: center; /* Centrar las columnas */
  width: 100%; /* Asegurar que no se desborde horizontalmente */
  box-sizing: border-box; /* Incluir el padding y el borde en el tamaño total */
}

.contactin .column {
  width: 100%; /* Hacer que las columnas ocupen todo el ancho en pantallas pequeñas */
  padding: 20px; /* Agregar espaciado interno */
  text-align: center; /* Centrar el contenido */
  box-sizing: border-box; /* Incluir el padding en el tamaño total */
}

.contactin h1,
.contactin p {
  font-size: 18px; /* Tamaño de fuente más pequeño */
  line-height: 1.4; /* Espaciado entre líneas más compacto */
  max-width: 100%; /* Ancho máximo del texto para evitar que se extienda demasiado */
  margin: 0 auto 20px; /* Centrar el texto horizontalmente */
  color: white;
}

.contactin form {
  text-align: left; /* Alinear el contenido del formulario a la izquierda */
  max-width: 100%; /* Limitar el ancho del formulario para que no se vea muy extendido */
  margin: 0 auto; /* Centrar el formulario horizontalmente */
  box-sizing: border-box; /* Incluir el padding en el tamaño total */
}

.contactin form label {
  display: block; /* Mostrar los labels en una línea nueva */
  margin-bottom: 8px; /* Añadir un poco de espacio entre los labels */
  color: white; /* Color del texto de los labels */
}

.contactin form input,
.contactin form textarea {
  width: 100%; /* Hacer que los campos de entrada y el área de texto ocupen todo el ancho disponible */
  padding: 10px; /* Añadir relleno a los campos de entrada */
  margin-bottom: 16px; /* Añadir espacio entre los campos */
  border: 1px solid #ddd; /* Añadir un borde */
  border-radius: 4px; /* Agregar bordes redondeados */
  background-color: #f9f9f9; /* Color de fondo del campo */
  color: #333; /* Color del texto */
  font-size: 16px; /* Tamaño de fuente */
  box-sizing: border-box; /* Incluir el padding en el tamaño total */
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
  margin-top: 20px; /* Agregar espacio encima de los iconos sociales */
}

.social-icons a {
  margin: 0 10px; /* Ajustar el margen entre los iconos según sea necesario */
  color: #fff; /* Color de los iconos */
  font-size: 50px; /* Tamaño más grande de los iconos */
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
  padding: 40px 20px; /* Reducir el padding para pantallas pequeñas */
  width: 100%; /* Asegurar que no se desborde horizontalmente */
  box-sizing: border-box; /* Incluir el padding en el tamaño total */
}

.mensaje-enviado h2 {
  color: white; /* Cambiar el color del texto a blanco */
  text-align: center; /* Asegurarse de que el texto esté centrado */
}

@media (min-width: 768px) {
  .contactin {
    flex-direction: row; /* Colocar las columnas en una fila en pantallas más grandes */
    justify-content: space-around; /* Espaciado entre las columnas */
    align-items: flex-start; /* Alinear los elementos al inicio verticalmente */
  }

  .contactin .column {
    flex: 1; /* Las columnas ocupan el mismo espacio */
    max-width: 30%; /* Limitar el ancho de las columnas */
    text-align: center; /* Centrar el contenido de las columnas */
    margin: 10px; /* Agregar margen entre columnas */
    box-sizing: border-box; /* Incluir el padding en el tamaño total */
  }

  .contactin h1,
  .contactin p {
    font-size: 24px; /* Tamaño de fuente mayor para pantallas grandes */
    max-width: 100%; /* Ancho máximo del texto */
  }

  .contactin form {
    max-width: 100%; /* Limitar el ancho del formulario */
  }
}
</style>
