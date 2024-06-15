<template>
  <template v-if="isAuthenticated">
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
          <template v-if="isAuthenticated">
            <i class="far fa-thumbs-up like-icon"> {{establecimiento.numLikes}}</i>
          </template>
          <!-- Mostrar solo el número de likes si el usuario no ha iniciado sesión -->
          <template v-else>
            <i class="far fa-thumbs-up like-icon"> {{establecimiento.numLikes}}</i>
          </template>
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
      <p class="subtitulo-nuevoEstablecimiento">Si has visitado un nuevo establecimiento que no figura en esta web, no te olvides de rellenar el formulario para añadir dicho establecimiento, así harás crecer aún más la comunidad!</p>
    </div>
    <router-link to="/nuevoEstablecimiento" class="cerrar-sesion-button2">FORMULARIO</router-link>
  </div>
  <contacto/>
  <footer-componente/>
</template>

<script>
import '@fortawesome/fontawesome-free/css/all.css';
import FooterComponente from "@/components/footer.vue";
import Header3 from "@/components/headerIniciadoSesion.vue";
import axios from 'axios';
import CabeceraComponente from "@/components/header.vue";
import { mapGetters } from "vuex";
import contacto from "@/components/contacto.vue";

export default {
  name: 'Vista-Inicio',
  components: {
    CabeceraComponente,
    Header3,
    FooterComponente,
    contacto
  },
  data() {
    return {
      establecimientos: [],
      establecimientosFiltrados: [],
      searchQuery: '',
      mensajeEnviado: false
    }
  },
  computed: {
    ...mapGetters(['isAuthenticated'])
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
  background-color: white;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center; /* Centra horizontalmente */
  text-align: center; /* Centra el texto */
}

.titulo-subtitulo-nuevoEstablecimiento {
  text-align: center; /* Centra el texto */
  max-width: 1000px; /* Ancho máximo del contenedor */
}

.titulo-nuevoEstablecimiento {
  font-size: 24px; /* Tamaño de fuente del título */
  line-height: 1.2; /* Espaciado entre líneas del título */
  margin-bottom: 10px; /* Espaciado inferior del título */
}

.subtitulo-nuevoEstablecimiento {
  font-size: 20px; /* Tamaño de fuente del subtítulo */
  line-height: 1.2; /* Espaciado entre líneas del subtítulo */
  margin-bottom: 0; /* Elimina el espaciado inferior del subtítulo */
}


.nombre {
  color: inherit; /* Utiliza el color heredado del elemento padre */
  text-decoration: none; /* Elimina el subrayado */
}

.nombre:hover {
  color: inherit; /* Utiliza el color heredado del elemento padre */
}

.mensaje-enviado h2 {
  color: white; /* Cambiar el color del texto a blanco */
  text-align: center; /* Asegurarse de que el texto esté centrado */
}

/* Media Queries para pantallas más pequeñas */
@media (max-width: 768px) {
  .container-principal-establecimientos {
    padding: 20px;
  }

  .titulo-subtitulo {
    padding: 0 10px; /* Añadir padding horizontal */
  }

  .barra-busqueda input {
    width: 100%; /* Ancho completo en pantallas pequeñas */
  }

  .establecimientos {
    grid-template-columns: 1fr; /* Una columna en pantallas pequeñas */
  }

  .titulo-nuevoEstablecimiento {
    font-size: 20px; /* Reducir tamaño de fuente */
  }

}
</style>
