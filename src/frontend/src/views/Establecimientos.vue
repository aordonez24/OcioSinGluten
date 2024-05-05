<template>
  <template v-if="token">
    <header3/>
  </template>
  <template v-else>
    <cabecera-componente/>
  </template>
  <div class="container-principal-establecimientos">
    <div class="titulo-subtitulo">
      <h1>Establecimientos sin gluten</h1>
      <p class="subtitulo-nuevoEstablecimiento">INTRODUZCA UNA CIUDAD, UNA CALLE, O EL NOMBRE DE UN ESTABLECIMIENTO, PARA BUSCAR ESTABLECIMIENTOS CON DISPONIBILIDAD DE COMIDA SIN GLUTEN</p>
    </div>

    <div class="barra-busqueda">
      <input type="text" placeholder="Buscar establecimiento..." v-model="searchQuery" @input="filterEstablecimientos">
    </div>

    <div class="establecimientos">
      <div v-for="establecimiento in establecimientosFiltrados" :key="establecimiento.idEstablecimiento" class="establecimiento">
        <h3>
          <router-link :to="'/verEstabecimiento/' + establecimiento.idEstablecimiento" class="nombre">{{ establecimiento.nombre }}</router-link>
          <i class="far fa-thumbs-up like-icon" @click="likeEstablecimiento(establecimiento)"></i>
        </h3>
        <p>{{ establecimiento.localidad }}, {{ establecimiento.provincia }}</p>
        <p>{{ establecimiento.calle }}</p>
        <p>{{ establecimiento.telefono }}</p>
      </div>
    </div>
  </div>
  <div class="nuevoEstablecimiento">
    <div class="titulo-subtitulo-nuevoEstablecimiento">
      <h1 class="titulo-nuevoEstablecimiento">¿Has encontrado un nuevo establecimiento con alimentación sin gluten y no aparece aquí?</h1>
      <p class="subtitulo-nuevoEstablecimiento">Si has visitado un nuevo establecimiento que no figura en esta web, no te olvides de rellenar el formulario para añadir dicho establecimiento, así harás crecer aún mas la comunidad!</p>
    </div>
    <router-link to="/nuevoEstablecimiento" class="cerrar-sesion-button2">FORMULARIO</router-link>
  </div>
    <div id="contacto" class="contactin">
      <div class="column">
        <h1>¿Tienes alguna pregunta sobre la celiaquía o los alimentos sin gluten?</h1>
        <p>¡Envíanos un mensaje y estaremos encantados de ayudarte!</p>
      </div>
      <div class="column">
        <form action="/submit-message" method="post">
          <label for="name">Nombre y apellidos:</label>
          <input type="text" id="name" name="name" required>
          <label for="email">Correo:</label>
          <input type="email" id="email" name="email" required>
          <label for="message">Escribe tu mensaje:</label>
          <textarea id="message" name="message" required></textarea>
          <button type="submit">Enviar</button>
        </form>
      </div>
      <div class="column">
        <p>¡También puedes seguirnos en nuestras redes sociales!</p>
        <div class="social-icons">
          <a href="#"><i class="fab fa-instagram"></i></a>
          <a href="#"><i class="fab fa-twitter"></i></a>
          <a href="#"><i class="fab fa-facebook"></i></a>
        </div>
      </div>
    </div>
  <footer-componente/>
</template>

<script>
import '@fortawesome/fontawesome-free/css/all.css';
import FooterComponente from "@/components/footer.vue";
import Header3 from "@/components/headerIniciadoSesion.vue";
import axios from 'axios';
import CabeceraComponente from "@/components/header.vue";

export default {
  name: 'Vista-Inicio',
  components: {
    CabeceraComponente,
    Header3,
    FooterComponente,
  },
  data() {
    return {
      token: localStorage.getItem('token'),
      establecimientos: [],
      establecimientosFiltrados: [],
      searchQuery: ''
    }
  },
  methods: {
    async cargarEstablecimientos() {
      try {
        const response = await axios.get('http://localhost:8080/ociosingluten/establecimientos/listadoEstablecimientos');
        this.establecimientos = response.data;
        console.log(this.establecimientos);
        this.establecimientosFiltrados = response.data; // Mostrar todos los establecimientos al principio
      } catch (error) {
        console.error('Error al cargar los establecimientos:', error);
      }
    },
    filterEstablecimientos() {
      // Filtrar establecimientos basado en la búsqueda
      this.establecimientosFiltrados = this.establecimientos.filter(establecimiento =>
          establecimiento.nombre.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          establecimiento.localidad.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          establecimiento.provincia.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
          establecimiento.calle.toLowerCase().includes(this.searchQuery.toLowerCase())
      );
    },
    async likeEstablecimiento(establecimiento) {
      // Lógica para manejar el "me gusta" del establecimiento
      // Puedes implementar la lógica aquí, por ejemplo, enviar una solicitud al servidor para guardar el "me gusta"
      console.log('Me gusta:', establecimiento.nombre);
    },
    async comentarEstablecimiento(idEstablecimiento) {
      // Lógica para manejar el "me gusta" del establecimiento
      // Puedes implementar la lógica aquí, por ejemplo, enviar una solicitud al servidor para guardar el "me gusta"
      console.log(idEstablecimiento);
    }
  },
  mounted() {
    this.cargarEstablecimientos();
  }
}
</script>

<style scoped>

.cerrar-sesion-button2 {
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: white;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  text-decoration: none; /* Elimina el subrayado */

}

.cerrar-sesion-button2:hover {
  background-color: #ffcc74;
}

.container-principal-establecimientos {
  padding: 30px;
  background-image: linear-gradient(120deg, #ffcc74 0%, #da1b60 100%);
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  border-radius: 10px; /* Bordes redondeados */
}

.titulo-subtitulo {
  text-align: center; /* Centra el texto */
}

.establecimientos {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); /* Columnas autoajustables */
  gap: 20px; /* Espacio entre establecimientos */
}

.establecimiento {
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 5px;
  background-color: #f9f9f9;
}

.nuevoEstablecimiento {
  padding: 30px;
  height: 300px; /* Ajusta esta altura según tus necesidades */
  background-color: white;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center; /* Centra horizontalmente */
  text-align: center; /* Centra el texto */
}

.titulo-subtitulo-nuevoEstablecimiento {
  text-align: center; /* Centra el texto */
  max-width: 1000px; /* Ancho máximo del contenedor

}

.titulo-nuevoEstablecimiento {
  font-size: 2400px; /* Tamaño de fuente del título */
  line-height: 1.2; /* Espaciado entre líneas del título */
  margin-bottom: 10px; /* Espaciado inferior del título */
}

.subtitulo-nuevoEstablecimiento {
  font-size: 20px; /* Tamaño de fuente del subtítulo */
  line-height: 1.2; /* Espaciado entre líneas del subtítulo */
  margin-bottom: 0; /* Elimina el espaciado inferior del subtítulo */
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

.barra-busqueda {
  margin-top: 20px; /* Espacio superior */
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

.like-icon {
  margin-left: 10px; /* Agrega un margen a la izquierda del icono */
}

.nombre {
  color: inherit; /* Utiliza el color heredado del elemento padre */
  text-decoration: none; /* Elimina el subrayado */
}

.nombre:hover {
  color: inherit; /* Utiliza el color heredado del elemento padre */
}
</style>