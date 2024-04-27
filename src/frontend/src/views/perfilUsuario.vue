<script setup>

import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";

</script>

<template>
  <header3/>
  <div class="container-principal">

  </div>
  <footer-componente/>
</template>

<script>
import axios from 'axios';

export default {
  name: 'PerfilUsuario',
  data() {
    return {
      usuario: null,
      loading: false,
      fotoPerfilUrl: ''
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

        // Convierte la imagen de bytes a una URL de imagen base64
        const base64String = btoa(String.fromCharCode.apply(null, this.usuario.fotoPerfil.data));
        const imageUrl = `data:${this.usuario.fotoPerfil.contentType};base64,${base64String}`;
        this.fotoPerfilUrl = imageUrl;

        this.loading = false;
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    }
  }
};
</script>


<style scoped>
.container-principal {
  width: 70vw; /* Ancho del viewport */
  height: 35vw;
  margin: 50px auto 100px; /* Centra el contenedor horizontalmente y deja un margen vertical de 50px arriba y 100px abajo */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
}
</style>