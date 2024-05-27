<template>
  <div>
    <header3/>
    <div class="container-principal">
      <div class="followers-column">
        <h2>Establecimientos favoritos del usuario</h2>
        <input type="text" v-model="favoritosSearchQuery" placeholder="Buscar establecimiento...">
        <div class="follower-list" ref="followerList">
          <div v-for="establecimiento in filtrarFavoritos" :key="establecimiento.nombre" class="follower">
            <div class="follower-info">
              <div>
                <p><router-link :to="'/verEstabecimiento/' + establecimiento.idEstablecimiento" class="nombre"> <strong>{{ establecimiento.nombre }}</strong></router-link></p>
                <p>{{ establecimiento.localidad }}, {{ establecimiento.provincia }}, {{ establecimiento.calle }}, {{ establecimiento.codPostal }}</p>
                <p>{{ establecimiento.telefono }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="following-column">
        <h2>Establecimientos visitados del usuario</h2>
        <input type="text" v-model="visitadosSearchQuery" placeholder="Buscar establecimiento visitado...">
        <div class="followed-list" ref="followedList">
          <div v-for="establecimiento in filtrarVisitados" :key="establecimiento.nombre" class="followed">
            <div class="followed-info">
              <div>
                <p><router-link :to="'/verEstabecimiento/' + establecimiento.idEstablecimiento" class="nombre"> <strong>{{ establecimiento.nombre }}</strong></router-link></p>
                <p>{{ establecimiento.localidad }}, {{ establecimiento.provincia }}, {{ establecimiento.calle }}, {{ establecimiento.codPostal }}</p>
                <p>{{ establecimiento.telefono }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <footer-componente />
  </div>
</template>

<script>
import axios from 'axios';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";

export default {
  components: { FooterComponente, Header3 },
  data() {
    return {
      establecimientosFavoritos: [],
      establecimientosVisitados: [],
      favoritosSearchQuery: '',
      visitadosSearchQuery: ''
    };
  },
  mounted() {
    this.obtenerEstablecimientosFavoritos();
    this.obtenerEstablecimientosVisitados();
  },
  computed: {
    filtrarFavoritos() {
      return this.establecimientosFavoritos.filter(est =>
          est.nombre.toLowerCase().includes(this.favoritosSearchQuery.toLowerCase())
      );
    },
    filtrarVisitados() {
      return this.establecimientosVisitados.filter(est =>
          est.nombre.toLowerCase().includes(this.visitadosSearchQuery.toLowerCase())
      );
    }
  },
  methods: {
    async obtenerEstablecimientosFavoritos() {
      try {
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/estFavoritos`);
        this.establecimientosFavoritos = response.data;
      } catch (error) {
        console.error('Error al obtener seguidores:', error);
      }
    },
    async obtenerEstablecimientosVisitados() {
      try {
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/estVisitados`);
        this.establecimientosVisitados = response.data;
      } catch (error) {
        console.error('Error al obtener seguidos:', error);
      }
    }
  },
};
</script>

<style scoped>
.container-principal {
  min-height: calc(100vh - 200px); /* Ajusta este valor según la altura de tu encabezado y pie de página */
  width: 80vw;
  margin: 50px auto 0; /* Eliminamos el margen inferior */
  padding: 45px;
  background-image: linear-gradient(120deg, #ffffff 0%, #ffcc74 100%);
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  display: flex;
  margin-bottom: 30px;
}

.followers-column, .following-column {
  flex: 1;
  padding: 20px;
}

.follower, .followed {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.follower-info, .followed-info {
  border: 1px solid #000000;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  align-items: center;
  width: 600px; /* Ajusta la altura mínima aquí */
}

.follower img, .followed img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 10px;
}

.following-column {
  margin-left: 20px;
}

.follower-list, .followed-list {
  overflow-y: auto; /* Barra de desplazamiento vertical */
}

input[type="text"] {
  width: 89%;
  padding: 8px;
  margin-bottom: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
}


footer-componente {
  position: fixed;
  bottom: 0;
  width: 100%;
}
.nombre {
  color: inherit; /* Utiliza el color heredado del elemento padre */
  text-decoration: none; /* Elimina el subrayado */
}

.nombre:hover {
  color: inherit; /* Utiliza el color heredado del elemento padre */
}
</style>