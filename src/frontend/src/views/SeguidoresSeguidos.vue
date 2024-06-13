<template>
  <div>
    <header3 />
    <div class="container1">
      <div class="container-principal">
      <div class="followers-column">
        <h2>Seguidores</h2>
        <input type="text" v-model="followerSearchQuery" placeholder="Buscar seguidor por username...">
        <div class="follower-list" ref="followerList">
          <div v-for="follower in filteredFollowers" :key="follower.username" class="follower">
            <div class="follower-info">
              <div class="profile-pic">
                <img v-if="follower.fotoPerfil && follower.fotoPerfil.length > 4" :src="'data:image/jpeg;base64,' + follower.fotoPerfil" :alt="'Foto de perfil de ' + follower.username">
                <i v-else class="fas fa-user"></i> <!-- Icono de usuario predeterminado -->
              </div>
              <div>
                <p @click="irAPerfil(follower.username)" class="username-link"><strong>{{ follower.username }}</strong></p>
                <p>{{ follower.nombre }} {{ follower.apellidos }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="following-column">
        <h2>Siguiendo</h2>
        <input type="text" v-model="followedSearchQuery" placeholder="Buscar usuario seguido por username...">
        <div class="followed-list" ref="followedList">
          <div v-for="followed in filteredFollowedUsers" :key="followed.username" class="followed">
            <div class="followed-info">
              <div class="profile-pic">
                <img v-if="followed.fotoPerfil.length > 4" :src="'data:image/jpeg;base64,' + followed.fotoPerfil" :alt="'Foto de perfil de ' + followed.username">
                <i v-else class="fas fa-user"></i> <!-- Icono de usuario predeterminado -->
              </div>
              <div>
                <p @click="irAPerfil(followed.username)" class="username-link"><strong>{{ followed.username }}</strong></p>
                <p>{{ followed.nombre }} {{ followed.apellidos }}</p>
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
import {mapGetters} from "vuex";


export default {
  components: { FooterComponente, Header3 },
  data() {
    return {
      followers: [],
      followedUsers: [],
      followerSearchQuery: '',
      followedSearchQuery: ''
    };
  },
  mounted() {
    this.obtenerSeguidores();
    this.obtenerSeguidos();
  },
  computed: {
    filteredFollowers() {
      return this.followers.filter(follower =>
          follower.username.toLowerCase().includes(this.followerSearchQuery.toLowerCase())
      );
    },
    filteredFollowedUsers() {
      return this.followedUsers.filter(followed =>
          followed.username.toLowerCase().includes(this.followedSearchQuery.toLowerCase())
      );
    },
    ...mapGetters(['username', 'isAuthenticated'])

  },
  methods: {
    async obtenerSeguidores() {
      try {
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidores`);
        this.followers = response.data;
      } catch (error) {
        console.error('Error al obtener seguidores:', error);
      }
    },
    async obtenerSeguidos() {
      try {
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidos`);
        this.followedUsers = response.data;
      } catch (error) {
        console.error('Error al obtener seguidos:', error);
      }
    },
    irAPerfil(username) {
      if(username === this.username) {
        this.$router.push({name: 'Perfil', params: {username: username}});
      }else{
        this.$router.push({name: 'perfilOtroUsuario', params: {username: username}});
      }
    },
  },

};
</script>

<style scoped>
.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat; /* Evitar que la imagen se repita */
  background-size: cover;
  padding: 20px;
}

.container-principal {
  min-height: calc(100vh - 200px); /* Ajusta este valor según la altura de tu encabezado y pie de página */
  width: 90vw;
  max-width: 1200px;
  margin: 50px auto 0;
  padding: 20px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  display: flex;
  flex-direction: column;
}

.followers-column, .following-column {
  padding: 20px;
  flex: 1;
  max-width: 100%;
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

footer-componente {
  position: fixed;
  bottom: 0;
  width: 100%;
}

.profile-pic {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background-color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  overflow: hidden;
}

.profile-pic img {
  width: 110%;
  height: 110%;
  object-fit: cover;
  object-position: center;
  border-radius: 50%;
}

.username-link {
  cursor: pointer;
  color: black;
  transition: color 0.5s;
}

.username-link:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .container-principal {
    padding: 10px; /* Ajuste el padding para que sea más compacto en pantallas pequeñas */
  }
  .followers-column, .following-column {
    width: 100%; /* Ambas columnas ocupan el 100% del ancho disponible */
    padding: 5px; /* Padding reducido para conservar espacio */
  }
  .profile-pic {
    width: 50px; /* Reduje el tamaño de la imagen de perfil para ajustarse mejor */
    height: 50px;
    border-radius: 50%; /* Añadí borde redondeado para mejorar aspecto en móviles */
  }
}

</style>
