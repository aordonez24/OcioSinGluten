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
  text-decoration: none;
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
  border-radius: 10px;
}

.titulo-subtitulo {
  text-align: center;
}

.establecimientos {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
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
  align-items: center;
  text-align: center;
}

.titulo-subtitulo-nuevoEstablecimiento {
  text-align: center;
  max-width: 1000px;
}

.titulo-nuevoEstablecimiento {
  font-size: 24px;
  line-height: 1.2;
  margin-bottom: 10px;
}

.subtitulo-nuevoEstablecimiento {
  font-size: 20px;
  line-height: 1.2;
  margin-bottom: 0;
}

.social-icons a:hover {
  color: #ffcc74;
}

.barra-busqueda {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.barra-busqueda input {
  width: 100%;
  max-width: 600px;
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
  box-sizing: border-box;
  text-align: center;
}

.like-icon {
  margin-left: 10px;
}

.nombre {
  color: inherit;
  text-decoration: none;
}

.nombre:hover {
  color: inherit;
}

.mensaje-enviado h2 {
  color: white;
  text-align: center;
}

@media (max-width: 768px) {
  .container-principal-establecimientos {
    padding: 20px;
  }

  .titulo-subtitulo {
    padding: 0 10px;
  }

  .barra-busqueda input {
    width: 100%;
  }

  .establecimientos {
    grid-template-columns: 1fr;
  }

  .titulo-nuevoEstablecimiento {
    font-size: 20px;
  }

}
</style>
