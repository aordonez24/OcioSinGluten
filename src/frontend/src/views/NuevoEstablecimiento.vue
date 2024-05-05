<script setup>

import CabeceraComponente2 from "@/components/header2.vue";
import FooterComponente from "@/components/footer.vue";
</script>

<template>
  <cabecera-componente2/>
  <div class="container-principal">
    <h1>¡Añade un nuevo establecimiento!</h1>
    <form @submit.prevent="agregarEstablecimiento" class="form-container">
      <div class="form-column">
        <label for="nombre">Nombre:</label>
        <input type="text" id="nombre" v-model="nombre">
        <label for="telefono">Teléfono:</label>
        <input type="number" id="telefono" v-model="telefono">
      </div>
      <div class="form-column">
        <label for="localidad">Localidad:</label>
        <input type="text" id="localidad" v-model="localidad">
        <label for="provincia">Provincia:</label>
        <input type="text" id="provincia" v-model="provincia">
        <label for="calle">Calle:</label>
        <input type="text" id="calle" v-model="calle">
      </div>
      <div class="form-column">
        <label for="codPostal">Código Postal:</label>
        <input type="number" id="codPostal" v-model="codPostal">
        <label for="pais">País:</label>
        <input type="text" id="pais" v-model="pais">
      </div>
    </form>
    <button @click="agregarEstablecimiento" class="botonagregar">Agregar Establecimiento</button>
  </div>
  <footer-componente/>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      nombre: '',
      telefono: null,
      localidad: '',
      provincia: '',
      calle: '',
      codPostal: null,
      pais: '',
      username:'',
    };
  },
  mounted() {
    this.username = localStorage.getItem('username');
  },
  methods: {
    async agregarEstablecimiento() {
      try {
        const formData = new FormData();
        formData.append('nombre', this.nombre);
        formData.append('telefono', this.telefono);
        formData.append('localidad', this.localidad);
        formData.append('provincia', this.provincia);
        formData.append('calle', this.calle);
        formData.append('codPostal', this.codPostal);
        formData.append('pais', this.pais);
        formData.append('username', this.username); // Obtener el nombre de usuario de la sesión actual

        const response = await axios.post('http://localhost:8080/ociosingluten/establecimientos/nuevoEstablecimiento', formData);

        console.log(response.data);

        // Reiniciar los campos del formulario después de enviar la solicitud
        this.nombre = '';
        this.telefono = null;
        this.localidad = '';
        this.provincia = '';
        this.calle = '';
        this.codPostal = null;
        this.pais = '';

        this.$router.push({ name: 'establecimientos'});

      } catch (error) {
        console.error('Error al agregar establecimiento:', error);
      }
    }

  }
};
</script>

<style scoped>

.container-principal {
  width: 60vw; /* Ancho del viewport */
  margin: 50px auto 100px; /* Centra el contenedor horizontalmente y deja un margen vertical de 50px arriba y 100px abajo */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
}

.form-container {
  display: flex;
  justify-content: space-between;
}

.form-column {
  flex: 1;
  margin-right: 20px; /* Espacio entre las columnas */
}

.form-column:last-child {
  margin-right: 0; /* Elimina el margen derecho de la última columna */
}

label {
  display: block;
  margin-bottom: 5px;
}

.botonagregar {
  background-color: #7F7F7F; /* Color de fondo del botón */
  color: white; /* Color del texto del botón */
  border: none; /* Eliminar el borde del botón */
  padding: 12px 24px; /* Aumentar el relleno del botón */
  cursor: pointer; /* Cambiar el cursor al pasar sobre el botón */
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */
  font-size: 16px; /* Tamaño de fuente */
  border-radius: 4px; /* Agregar bordes redondeados */
  transition: background-color 0.3s ease; /* Agregar una transición suave al color de fondo */
  display: block; /* Convertir el botón en un elemento de bloque */
  margin: 0 auto; /* Centrar horizontalmente */
  margin-top: 20px;
}

.botonagregar:hover {
  background-color: #ffcc74; /* Cambiar el color de fondo al pasar el cursor sobre el botón */
}

</style>