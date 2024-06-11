<template>
  <header3/>
  <div class="container-principal">
    <h2 class="welcome-text">¡Bienvenido a la comunidad de Ocio Sin Gluten!</h2>
    <p class="subtext">Busque a conocidos y comparta experiencias con ellos</p>
    <button @click="iniciarChat" class="cerrar-sesion-button">Acceda a nuestro chat grupal!</button>
    <div class="barra-busqueda">
      <input type="text" v-model="searchQuery" placeholder="Buscar usuario..." @input="filterUsers">
    </div>
    <div class="user-grid">
      <div v-for="user in filteredUsers" :key="user.username" class="user-card">
        <div class="user-avatar">
          <img v-if="user.fotoPerfil" :src="'data:image/jpeg;base64,' + user.fotoPerfil"  />
          <i v-else class="fas fa-user"></i>
        </div>
        <div class="user-info">
          <div>
            <p @click="irAPerfil(user.username)" class="username-link"><strong>{{ user.username }}</strong></p>
          </div>
          <div>{{ user.nombre }} {{ user.apellidos }}</div>
        </div>
      </div>
    </div>
  </div>
  <div id="contacto" class="contactin" v-if="!mensajeEnviado">
    <div class="column">
      <h1>¿Tienes alguna pregunta sobre la celiaquía o los alimentos sin gluten?</h1>
      <p>¡Envíanos un mensaje y estaremos encantados de ayudarte!</p>
    </div>
    <div class="column" v-if="!mensajeEnviado">
      <form @submit.prevent="handleSubmit">
        <label for="name">Nombre y apellidos:</label>
        <input type="text" id="name" v-model="name" required>
        <label for="email">Correo:</label>
        <input type="email" id="email" v-model="email" required>
        <label for="message">Escribe tu mensaje:</label>
        <textarea id="message" v-model="message" required></textarea>
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
  <footer-componente/>
</template>

<script>
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import axios from 'axios';
import {mapGetters} from "vuex";


export default {
  name: 'comunidad-ociosingluten',
  components: {FooterComponente, Header3},
  data() {
    return {
      users: [], // Aquí se almacenarán todos los usuarios
      filteredUsers: [], // Lista de usuarios filtrados por la barra de búsqueda
      searchQuery: '', // Almacena el texto ingresado en la barra de búsqueda
      rol:''
    };
  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  mounted() {
    this.obtenerUsuarios();
  },
  methods: {
    filterUsers() {
      const loggedInUsername = this.username;
      this.filteredUsers = this.users.filter(user =>
          user.username.toLowerCase().includes(this.searchQuery.toLowerCase()) && user.username !== loggedInUsername
      );
    },
    async obtenerUsuarios() {
      const yo = this.username;
      const response2 =  await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${yo}`);
      this.rol = response2.data.rol;
      let response = "";
      if (this.rol === 'ADMIN') {
        response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/listadoUsuariosadmin`);
      } else {
        response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/listadoUsuarioscomun`);
      }
      this.users = response.data;
      this.filterUsers();
    },
    irAPerfil(username) {
      this.$router.push({ name: 'perfilOtroUsuario', params: { username: username } });
    },
    iniciarChat() {
      this.$router.push({ name: 'chatGrupal'}); // Navegar al chat con ese usuario
    },
    handleSubmit() {
      if(!this.validateForm()){
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
            // Manejar errores en caso de que la solicitud falle
            console.error('Error al enviar el mensaje:', error);
          });
    },
    validateForm() {
      if (!this.name || !this.email || !this.message) {
        alert('Todos los campos son obligatorios.');
        return false;
      }
    },
  }
};
</script>

<style scoped>

.container-principal {
  height: 100vh; /* 100% del alto de la pantalla */
  width: 100%; /* 100% del ancho de la pantalla */
  background-color: #ffcc74;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-text {
  font-size: 30px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

/* Estilos para el subtítulo */
.subtext {
  font-size: 24px;
  text-align: center;
  margin-bottom: 20px;
}


.search-input {
  margin-bottom: 20px; /* Espacio entre la barra de búsqueda y los usuarios */
  width: 50%; /* Ancho de la barra de búsqueda */
}

.user-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.user-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px; /* Tamaño del icono */
  background-color: #ccc; /* Color de fondo predeterminado */
  color: white; /* Color del icono */
  margin-right: 30px;
}

.username-link {
  cursor: pointer; /* Cambia el cursor al pasar el ratón */
  color: black; /* Cambia el color del texto */
}

.user-avatar img {
  width: 100px;
  height: 100px;
  border-radius: 50%; /* Hacer que la imagen sea circular */
  object-fit: cover; /* Hacer que la imagen se ajuste dentro del contenedor manteniendo su relación de aspecto */
}

.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.user-card:not(:last-child) {
  margin-right: 20px; /* Espacio entre cada tarjeta de usuario */
}


.user-info {
  text-align: center;
  margin-bottom: 10px;
}

.seguir-boton {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.seguir-boton:hover {
  background-color: #ffcc74;
}

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

.cerrar-sesion-button {
  padding: 8px 12px;
  border-radius: 20px;
  margin-bottom: 20px; /* Espacio entre el botón y la barra de búsqueda */
  color: black;
  background-color: white;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
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

.barra-busqueda {
  margin-top: 10px; /* Espacio superior */
  margin-bottom: 20px; /* Espacio inferior */
  display: flex;
  justify-content: center;
  align-items: center; /* Centra horizontalmente */
}

.barra-busqueda input {
  width: 600px; /* Ancho de la barra de búsqueda */
  padding: 10px; /* Espacio interno */
  border-radius: 5px; /* Bordes redondeados */
  border: 1px solid #ccc; /* Borde */
  box-sizing: border-box; /* Incluir padding y border en el ancho */
  text-align: center; /* Centrar el texto */
}
</style>
