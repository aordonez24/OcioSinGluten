
<template>
  <header3/>
  <div class="container-principal">
    <img v-if="fotoPerfilURL" :src="'data:image/jpeg;base64,' + fotoPerfilURL" alt="Foto de perfil">
    <p v-else>No hay foto de perfil disponible</p>
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
</style>