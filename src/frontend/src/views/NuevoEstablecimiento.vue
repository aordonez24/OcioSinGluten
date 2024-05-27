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
        <input type="text" id="nombre" v-model="nombre" :class="{ 'is-invalid': !nombreValido }">
        <span v-if="!nombreValido" class="text-danger">Nombre no válido (máximo 30 caracteres).</span>
        <label for="telefono">Teléfono:</label>
        <input type="number" id="telefono" v-model="telefono" :class="{ 'is-invalid': !telefonoValido }">
        <span v-if="!telefonoValido" class="text-danger">Teléfono no válido (9 dígitos).</span>
      </div>
      <div class="form-column">
        <label for="localidad">Localidad:</label>
        <input type="text" id="localidad" v-model="localidad" :class="{ 'is-invalid': !localidadValida }">
        <span v-if="!localidadValida" class="text-danger">Localidad no válida.</span>
        <label for="provincia">Provincia:</label>
        <input type="text" id="provincia" v-model="provincia" :class="{ 'is-invalid': !provinciaValida }">
        <span v-if="!provinciaValida" class="text-danger">Provincia no válida.</span>
        <label for="calle">Calle:</label>
        <input type="text" id="calle" v-model="calle" :class="{ 'is-invalid': !calleValida }">
        <span v-if="!calleValida" class="text-danger">Calle no válida.</span>
      </div>
      <div class="form-column">
        <label for="codPostal">Código Postal:</label>
        <input type="number" id="codPostal" v-model="codPostal" :class="{ 'is-invalid': !codPostalValido }">
        <span v-if="!codPostalValido" class="text-danger">Código Postal no válido.</span>
        <label for="pais">País:</label>
        <input type="text" id="pais" v-model="pais" :class="{ 'is-invalid': !paisValido }">
        <span v-if="!paisValido" class="text-danger">País no válido.</span>
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
      username: '',
      nombreValido: true,
      telefonoValido: true,
      localidadValida: true,
      provinciaValida: true,
      calleValida: true,
      codPostalValido: true,
      paisValido: true
    };
  },
  mounted() {
    this.username = localStorage.getItem('username');
  },
  methods: {
    validarFormulario() {
      this.nombreValido = this.nombre.length > 0 && this.nombre.length <= 30;
      this.telefonoValido = /^\d{9}$/.test(this.telefono);
      this.localidadValida = this.localidad.length > 0;
      this.provinciaValida = this.provincia.length > 0;
      this.calleValida = this.calle.length > 0;
      this.codPostalValido = /^\d+$/.test(this.codPostal);
      this.paisValido = this.pais.length > 0;

      return this.nombreValido && this.telefonoValido && this.localidadValida &&
          this.provinciaValida && this.calleValida && this.codPostalValido && this.paisValido;
    },
    async agregarEstablecimiento() {
      if (!this.validarFormulario()) {
        console.error('Error en la validación del formulario');
        return;
      }

      try {
        const formData = new FormData();
        formData.append('nombre', this.nombre);
        formData.append('telefono', this.telefono);
        formData.append('localidad', this.localidad);
        formData.append('provincia', this.provincia);
        formData.append('calle', this.calle);
        formData.append('codPostal', this.codPostal);
        formData.append('pais', this.pais);
        formData.append('username', this.username);

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

        this.$router.push({ name: 'establecimientos' });

      } catch (error) {
        console.error('Error al agregar establecimiento:', error);
      }
    }
  }
};
</script>

<style scoped>
.container-principal {
  width: 60vw;
  margin: 50px auto 100px;
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
  margin-right: 20px;
}

.form-column:last-child {
  margin-right: 0;
}

label {
  display: block;
  margin-bottom: 5px;
}

.botonagregar {
  background-color: #7F7F7F;
  color: white;
  border: none;
  padding: 12px 24px;
  cursor: pointer;
  font-family: 'Montserrat', sans-serif;
  font-size: 16px;
  border-radius: 4px;
  transition: background-color 0.3s ease;
  display: block;
  margin: 0 auto;
  margin-top: 20px;
}

.botonagregar:hover {
  background-color: #ffcc74;
}

.is-invalid {
  border-color: red;
}

.text-danger {
  color: red;
}
</style>
