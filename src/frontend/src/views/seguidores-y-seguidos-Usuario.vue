<template>
  <div>
    <header3 />
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
                <button class="seguir-boton" @click="seguirUsuario(follower.username)" v-if="!yaSiguiendo(follower)">Seguir</button>
                <button class="seguir-boton" disabled v-else>Se siguen mutuamente!</button>
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
    }
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
    async seguirUsuario(alquesigo) {
      try {
        const yo = localStorage.getItem('username');
        const data = {
          usernameQueSigueA: yo,
          usernameAQuienSigue: alquesigo // Asegúrate de obtener el nombre de usuario del objeto alquesigo
        };

        const response = await axios.post(
            `http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${yo}/nuevoSeguidor`,
            data
        );
        if (response.status === 200) {
          window.location.reload();
        }
      } catch (error) {
        console.error('Error al seguir al usuario:', error);
      }
    }
    ,
    yaSiguiendo(follower) {
      return this.followedUsers.some(followed => followed.username === follower.username);
    },
    irAPerfil(username) {
      this.$router.push({ name: 'perfilOtroUsuario', params: { username: username } });
    }
  },
};
</script>

<style scoped>
.container-principal {
  min-height: calc(100vh - 200px); /* Ajusta este valor según la altura de tu encabezado y pie de página */
  width: 70vw;
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
  width: 100%;
  padding: 8px;
  margin-bottom: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
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
  background-color: #ffa500;
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
  transition: color 0.5s; /* Agrega una transición para un cambio suave de color */
}

.username-link:hover {
  text-decoration: #ffa500; /* Subraya el texto al pasar el ratón */
}

</style>