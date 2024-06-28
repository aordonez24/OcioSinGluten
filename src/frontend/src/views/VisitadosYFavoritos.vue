<template>
  <div>
    <header3/>
    <div class="container1">
      <div class="container-principal">
        <div class="followers-column">
          <h2>Establecimientos favoritos del usuario</h2>
          <input type="text" v-model="favoritosSearchQuery" placeholder="Buscar establecimiento...">
          <div class="follower-list" ref="followerList">
            <div v-for="establecimiento in filtrarFavoritos" :key="establecimiento.nombre" class="follower">
              <div class="follower-info">
                <div>
                  <p><router-link :to="'/verEstabecimiento/' + establecimiento.idEstablecimiento" class="nombre"><strong>{{ establecimiento.nombre }}</strong></router-link></p>
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
                  <p><router-link :to="'/verEstabecimiento/' + establecimiento.idEstablecimiento" class="nombre"><strong>{{ establecimiento.nombre }}</strong></router-link></p>
                  <p>{{ establecimiento.localidad }}, {{ establecimiento.provincia }}, {{ establecimiento.calle }}, {{ establecimiento.codPostal }}</p>
                  <p>{{ establecimiento.telefono }}</p>
                </div>
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
      const query = this.favoritosSearchQuery.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
      return this.establecimientosFavoritos.filter(est =>
          this.matchEstablecimiento(est, query)
      );
    },
    filtrarVisitados() {
      const query = this.visitadosSearchQuery.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
      return this.establecimientosVisitados.filter(est =>
          this.matchEstablecimiento(est, query)
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
        console.error('Error al obtener establecimientos favoritos:', error);
      }
    },
    async obtenerEstablecimientosVisitados() {
      try {
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/estVisitados`);
        this.establecimientosVisitados = response.data;
      } catch (error) {
        console.error('Error al obtener establecimientos visitados:', error);
      }
    },
    matchEstablecimiento(establecimiento, query) {
      const normalizedName = establecimiento.nombre.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
      const normalizedLocalidad = establecimiento.localidad.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
      const normalizedProvincia = establecimiento.provincia.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");
      const normalizedCalle = establecimiento.calle.toLowerCase().normalize("NFD").replace(/[\u0300-\u036f]/g, "");

      return (
          normalizedName.includes(query) ||
          normalizedLocalidad.includes(query) ||
          normalizedProvincia.includes(query) ||
          normalizedCalle.includes(query)
      );
    }
  }
};
</script>


<style scoped>
.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat;
  background-size: cover;
  padding: 20px;
}

.container-principal {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: calc(100vh - 200px);
  width: 90vw;
  max-width: 1200px;
  margin: 50px auto 0;
  padding: 20px;
  background-image: linear-gradient(120deg, #ffffff 0%, #ffcc74 100%);
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
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
  border: 1px solid #ccc;
  border-radius: 10px;
  padding: 10px;
  display: flex;
  align-items: center;
  width: 100%;
}

.follower img, .followed img {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  margin-right: 10px;
}

.follower-list, .followed-list {
  overflow-y: auto;
}

input[type="text"] {
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
}

.nombre {
  color: inherit;
  text-decoration: none;
}

.nombre:hover {
  color: inherit;
}

@media (max-width: 768px) {
  .container-principal {
    flex-direction: column;
  }
  .followers-column, .following-column {
    width: 100%;
    margin-bottom: 20px;
  }
}
</style>
