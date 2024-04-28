
<template>
  <header3/>
  <div class="container-principal">
    <div class="profile-picture-container">
      <img
          v-if="fotoPerfilURL"
          :src="'data:image/jpeg;base64,' + fotoPerfilURL"
          alt="Foto de perfil"
          class="profile-picture" />
      <p v-else>No hay foto de perfil disponible</p>
      <button @click="changeProfilePicture" class="change-profile-picture-button">Cambiar foto de perfil</button>
      <button @click="cerrarSesion" class="cerrar-sesion-button">Cerrar sesión</button>
    </div>
  </div>
  <footer-componente/>
</template>

<script>
import axios from 'axios';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";

export default {
  name: 'PerfilUsuario',
  components: {
    Header3,
    FooterComponente
  },
  data() {
    return {
      usuario: null,
      loading: false,
      fotoPerfilURL: null
    };
  },
  mounted() {
    this.obtenerUsuario();
  },
  methods: {
    async obtenerUsuario() {
      try {
        this.loading = true;

        const username = this.$route.params.username;

        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${username}`);

        this.usuario = response.data;

        this.fotoPerfilURL = response.data.fotoPerfil;


        console.log(response.data.fotoPerfil);
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false; // Desactivar el estado de carga en caso de error
      }
    },
    async cerrarSesion() {
      try {
        // Cerrar sesión aquí (por ejemplo, eliminar el token de autenticación)
        localStorage.removeItem('token');

        // Navegar a la ruta "/"
        this.$router.push('/');
      } catch (error) {
        console.error('Error al cerrar sesión:', error);
      }
    }
  }
};
</script>

<style scoped>
.container-principal {
  width: 70vw;
  height: 35vw;
  margin: 50px auto 100px;
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
}

.profile-picture-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.profile-picture {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 20px;
}

.change-profile-picture-button{
  text-decoration: none;
  display: block;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  align-self: flex-start;
  margin-left: 0;
}

.change-profile-picture-button:hover {
  background-color: #9DD9D2;
  border-color: transparent;
}

.cerrar-sesion-button {
  text-decoration: none;
  display: block;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  margin-top: 325px;
  justify-content: flex-end; /* Colocar el botón abajo */
  align-self: flex-start; /* Forzar el botón a la parte superior del contenedor */
}

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
  border-color: transparent;
}
</style>