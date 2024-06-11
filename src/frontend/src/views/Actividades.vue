<template>
  <div class="page-container">
    <header3 />
    <div class="container-principal">
      <h1>Actividades de Ocio Sin Gluten</h1>
      <p class="intro-text">
        Si quiere que aparezca alguna de sus actividades por aquí, tan solo tiene que comentar algún establecimiento,
        marcarlo como visitado, dejar un like o incluso añadir un establecimiento que no figure aún en Ocio Sin Gluten!
      </p>
      <div class="barra-busqueda">
        <input v-model="searchQuery" placeholder="Buscar actividades por nombre de usuario" class="search-input" />
      </div>
      <div v-for="actividad in filteredActividades" :key="actividad.id" class="actividad">
        <div class="user-avatar">
          <img v-if="actividad.autor.fotoPerfil" :src="'data:image/jpeg;base64,' + actividad.autor.fotoPerfil" class="perfil" alt="Foto de perfil" @click="irAPerfil(actividad.autor.username)" />
          <i v-else class="fas fa-user default-icon" @click="irAPerfil(actividad.autor.username)"></i>
        </div>
        <div class="contenido-actividad">
          <div>
            <strong @click="irAPerfil(actividad.autor.username)" class="nombre-usuario">{{ actividad.autor.username }}</strong>
            : {{ traducirMensaje(actividad.mensajePredefinido) }} {{ actividad.establecimiento.nombre }}, {{actividad.establecimiento.localidad }}, {{actividad.establecimiento.provincia }}
          </div>
          <div class="fecha">{{ actividad.fechaContribucion }}</div>
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
          <input type="text" id="name" v-model="namelon" required>
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
          <a href="#"><i class="fab fa-instagram"></i></a>
          <a href="#"><i class="fab fa-twitter"></i></a>
          <a href="#"><i class="fab fa-facebook"></i></a>
        </div>
      </div>
    </div>
    <div v-else class="contactin mensaje-enviado">
      <h2>¡Mensaje enviado, en breves obtendrás respuestas!</h2>
    </div>
    <footer-componente />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import { useRouter } from 'vue-router';
import {mapGetters} from "vuex";

export default {
  components: { Header3, FooterComponente },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  setup() {
    const actividades = ref([]);
    const searchQuery = ref('');
    const router = useRouter();
    const mensajeEnviado = ref(false); // Agregar ref para mensajeEnviado
    const namelon = ref(''); // Agregar ref para namelon
    const email = ref(''); // Agregar ref para email
    const message = ref(''); // Agregar ref para message



    const mensajeMap = {
      HA_VISITADO: 'Ha visitado',
      HA_DADO_LIKE: 'Ha dado like a',
      HA_COMENTADO: 'Ha comentado en',
      HA_PUBLICADO: 'Ha publicado un nuevo establecimiento sin gluten llamado: '
    };

    const fetchActividades = async () => {
      try {
        const response = await axios.get('http://localhost:8080/ociosingluten/actividades/listadoActividades');
        actividades.value = response.data.sort((a, b) => new Date(b.fechaContribucion) - new Date(a.fechaContribucion));
      } catch (error) {
        console.error('Error fetching actividades:', error);
      }
    };

    const handleSubmit = () => {
      // Envío del formulario al servidor
      axios.post('http://localhost:8080/ociosingluten/quejas/nuevaQueja', {
        nombre: namelon.value, // Acceder a los valores con .value
        email: email.value, // Acceder a los valores con .value
        mensaje: message.value // Acceder a los valores con .value
      })
          .then(response => {
            console.log('Mensaje enviado con éxito:', response.data);
            mensajeEnviado.value = true;
          })
          .catch(error => {
            // Manejar errores en caso de que la solicitud falle
            console.error('Error al enviar el mensaje:', error);
          });
    }

    const traducirMensaje = (mensaje) => {
      return mensajeMap[mensaje] || mensaje;
    };

    const irAPerfil = (username) => {
      if(username === this.username) {
        router.push({name: 'Perfil', params: {username: username}});
      }else{
        router.push({name: 'perfilOtroUsuario', params: {username: username}});
      }
    };

    const filteredActividades = computed(() => {
      if (!searchQuery.value) {
        return actividades.value;
      }
      return actividades.value.filter(actividad =>
          actividad.autor.username.toLowerCase().includes(searchQuery.value.toLowerCase())
      );
    });

    onMounted(() => {
      fetchActividades();
    });

    return {
      actividades,
      searchQuery,
      traducirMensaje,
      irAPerfil,
      filteredActividades,
      handleSubmit,
      mensajeEnviado, // Agregar mensajeEnviado al objeto de retorno
      namelon, // Agregar namelon al objeto de retorno
      email, // Agregar email al objeto de retorno
      message // Agregar message al objeto de retorno
    };
  }
};
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container-principal {
  flex: 1;
  min-height: calc(80vh - 200px);
  width: 90%;
  margin: 40px auto;
  padding: 30px;
  background-image: linear-gradient(120deg, #ffffff 0%, #ffcc74 100%);
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #000000;
}

.intro-text {
  max-width: 600px;
  margin: 0 auto 20px;
  text-align: center;
}

.search-input {
  width: 100%;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
}

footer-componente {
  margin-top: 20px;
}

.actividad {
  border: 1px solid #000000;
  padding: 10px;
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.user-avatar {
  margin-right: 10px;
}

.perfil {
  border-radius: 50%;
  width: 50px;
  height: 50px;
}

.default-icon {
  font-size: 50px;
  color: #ccc;
  cursor: pointer;
}

.fecha {
  margin-top: 5px;
}

.barra-busqueda {
  margin-top: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.search-input {
  width: 50%;
  max-width: 600px;
  padding: 10px;
  margin-bottom: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  box-sizing: border-box;
}
</style>
