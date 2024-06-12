<template>
  <div>
    <header3 />
    <div class="container1">
      <div class="container-principal">
      <div class="followers-column">
        <h2>Seguidores</h2>
        <input type="text" v-model="followerSearchQuery" placeholder="Buscar seguidor...">
        <div class="follower-list" ref="followerList">
          <div v-for="follower in filteredFollowers" :key="follower.username" class="follower">
            <div class="follower-info">
              <div class="profile-pic">
                <img v-if="follower.fotoPerfil" :src="'data:image/jpeg;base64,' + follower.fotoPerfil" :alt="'Foto de perfil de ' + follower.username">
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
        <input type="text" v-model="followedSearchQuery" placeholder="Buscar usuario seguido...">
        <div class="followed-list" ref="followedList">
          <div v-for="followed in filteredFollowedUsers" :key="followed.username" class="followed">
            <div class="followed-info">
              <div class="profile-pic">
                <img v-if="followed.fotoPerfil" :src="'data:image/jpeg;base64,' + followed.fotoPerfil" :alt="'Foto de perfil de ' + followed.username">
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
  width: 75vw;
  margin: 50px auto 0; /* Eliminamos el margen inferior */
  padding: 45px;
  background-color: #fff;
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
  border: 1px solid #ccc;
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
  width: 96%;
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
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background-color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 30px;
  overflow: hidden;
}

.profile-pic img {
  width: 110%;
  height: 110%;
  object-fit: cover; /* Ajusta la imagen al contenedor, recortando si es necesario */
  object-position: center; /* Añade esta línea */
  border-radius: 50%; /* Mantén el borde circular */
}

.username-link {
  cursor: pointer; /* Cambia el cursor al pasar el ratón */
  color: black; /* Cambia el color del texto */
  transition: color 0.5s; /* Agrega una transición para un cambio suave de color */
}

.username-link:hover {
  text-decoration: #ffcc74; /* Subraya el texto al pasar el ratón */
}


</style>