<script setup>

import CabeceraComponente2 from "@/components/header2.vue";
import FooterComponente from "@/components/footer.vue";
</script>


<template>
  <cabecera-componente2/>
  <div class="container1">
    <div class="container-principal">
      <h1>¡Añade un nuevo establecimiento!</h1>
      <form @submit.prevent="agregarEstablecimiento" class="form-container">
        <div class="form-group">
          <label for="nombre">Nombre:</label>
          <input type="text" id="nombre" v-model="nombre" @input="validarNombre" maxlength=30 class="form-control" :class="{ 'is-invalid': !nombreValido }">
          <span v-if="!nombreValido" class="text-danger">Nombre no válido (máximo 30 caracteres).</span>
        </div>
        <div class="form-group">
          <label for="pais">País:</label>
          <select id="pais" v-model="pais" class="form-control" @change="paisSeleccionado">
            <option value="España">España</option>
          </select>
        </div>

        <!-- Campo de provincia -->
        <div v-if="mostrarProvincia" class="form-group">
          <label for="provincia">Provincia:</label>
          <select id="provincia" v-model="provincia" class="form-control" @change="provinciaSeleccionada">
            <option v-for="prov in provincias" :key="prov.nombre" :value="prov.nombre">{{ prov.nombre }}</option>
          </select>
        </div>

        <!-- Campo de localidad -->
        <div v-if="mostrarLocalidad" class="form-group">
          <label for="localidad">Localidad:</label>
          <select id="localidad" v-model="localidad" class="form-control">
            <option v-for="loc in provincias.find(p => p.nombre === provincia).localidades" :key="loc">{{ loc }}</option>
          </select>
        </div>

        <div class="form-group">
          <label for="telefono">Teléfono:</label>
          <input type="number" id="telefono" v-model="telefono" @input="validarTelefono" class="form-control" :class="{ 'is-invalid': !telefonoValido }">
          <span v-if="!telefonoValido" class="text-danger">Teléfono no válido (9 dígitos).</span>
        </div>

        <div class="form-group">
          <label for="calle">Calle:</label>
          <input type="text" id="calle" v-model="calle" @input="validarCalle" class="form-control" maxlength=50 :class="{ 'is-invalid': !calleValida }">
          <span v-if="!calleValida" class="text-danger">Calle no válida.</span>
        </div>

        <div class="form-group">
          <label for="codPostal">Código Postal:</label>
          <input type="text" id="codPostal" v-model="codPostal" @input="validarCodPostal" class="form-control" :class="{ 'is-invalid': !codPostalValido }">
          <span v-if="!codPostalValido" class="text-danger">Código Postal no válido.</span>
        </div>

        <button type="submit" class="btn-primary">Agregar Establecimiento</button>
      </form>
    </div>
  </div>
  <footer-componente/>
</template>


<script>
import axios from 'axios';
import {mapGetters} from "vuex";

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
      nombreValido: true,
      telefonoValido: true,
      localidadValida: true,
      provinciaValida: true,
      calleValida: true,
      codPostalValido: true,
      paisValido: true,
      provincias: [
        { nombre: 'Álava', localidades: ['Vitoria-Gasteiz', 'Llodio', 'Amurrio'] },
        { nombre: 'Albacete', localidades: ['Albacete', 'Hellín', 'Villarrobledo'] },
        { nombre: 'Alicante', localidades: ['Alicante', 'Elche', 'Torrevieja'] },
        { nombre: 'Almería', localidades: ['Almería', 'Roquetas de Mar', 'El Ejido'] },
        { nombre: 'Asturias', localidades: ['Oviedo', 'Gijón', 'Avilés'] },
        { nombre: 'Ávila', localidades: ['Ávila', 'Arévalo', 'Las Navas del Marqués'] },
        { nombre: 'Badajoz', localidades: ['Badajoz', 'Mérida', 'Don Benito'] },
        { nombre: 'Barcelona', localidades: ['Barcelona', 'Hospitalet de Llobregat', 'Badalona'] },
        { nombre: 'Burgos', localidades: ['Burgos', 'Miranda de Ebro', 'Aranda de Duero'] },
        { nombre: 'Cáceres', localidades: ['Cáceres', 'Plasencia', 'Navalmoral de la Mata'] },
        { nombre: 'Cádiz', localidades: ['Cádiz', 'Jerez de la Frontera', 'Algeciras'] },
        { nombre: 'Cantabria', localidades: ['Santander', 'Torrelavega', 'Camargo'] },
        { nombre: 'Castellón', localidades: ['Castellón de la Plana', 'Villarreal', 'Burriana'] },
        { nombre: 'Ceuta', localidades: ['Ceuta'] },
        { nombre: 'Ciudad Real', localidades: ['Ciudad Real', 'Puertollano', 'Tomelloso'] },
        { nombre: 'Córdoba', localidades: ['Córdoba', 'Lucena', 'Puente Genil'] },
        { nombre: 'Cuenca', localidades: ['Cuenca', 'Tarancón', 'San Clemente'] },
        { nombre: 'Gerona', localidades: ['Gerona', 'Figueras', 'Blanes'] },
        { nombre: 'Granada', localidades: ['Granada', 'Motril', 'Almuñécar'] },
        { nombre: 'Guadalajara', localidades: ['Guadalajara', 'Azurina', 'Alovera'] },
        { nombre: 'Guipúzcoa', localidades: ['San Sebastián', 'Irún', 'Éibar'] },
        { nombre: 'Huelva', localidades: ['Huelva', 'Lepe', 'Almonte'] },
        { nombre: 'Huesca', localidades: ['Huesca', 'Monzón', 'Fraga'] },
        { nombre: 'Islas Baleares', localidades: ['Palma de Mallorca', 'Ibiza', 'Manacor'] },
        { nombre: 'Jaén', localidades: ['Jaén', 'Linares', 'Andújar'] },
        { nombre: 'La Coruña', localidades: ['La Coruña', 'Santiago de Compostela', 'Ferrol'] },
        { nombre: 'La Rioja', localidades: ['Logroño', 'Calahorra', 'Arnedo'] },
        { nombre: 'Las Palmas', localidades: ['Las Palmas de Gran Canaria', 'Telde', 'Santa Lucía'] },
        { nombre: 'León', localidades: ['León', 'Ponferrada', 'San Andrés del Rabanedo'] },
        { nombre: 'Lérida', localidades: ['Lérida', 'Mollerusa', 'Seo de Urgel'] },
        { nombre: 'Lugo', localidades: ['Lugo', 'Villalba', 'Monforte de Lemos'] },
        { nombre: 'Madrid', localidades: ['Madrid', 'Móstoles', 'Alcalá de Henares'] },
        { nombre: 'Málaga', localidades: ['Málaga', 'Marbella', 'Mijas'] },
        { nombre: 'Melilla', localidades: ['Melilla'] },
        { nombre: 'Murcia', localidades: ['Murcia', 'Cartagena', 'Lorca'] },
        { nombre: 'Navarra', localidades: ['Pamplona', 'Tudela', 'Estella'] },
        { nombre: 'Orense', localidades: ['Orense', 'Carballino', 'Verín'] },
        { nombre: 'Palencia', localidades: ['Palencia', 'Aguilar de Campoo', 'Guardo'] },
        { nombre: 'Pontevedra', localidades: ['Pontevedra', 'Vigo', 'Villagarcía de Arosa'] },
        { nombre: 'Salamanca', localidades: ['Salamanca', 'Ciudad Rodrigo', 'Béjar'] },
        { nombre: 'Segovia', localidades: ['Segovia', 'Cuéllar', 'La Cuesta'] },
        { nombre: 'Sevilla', localidades: ['Sevilla', 'Dos Hermanas', 'Alcalá de Guadaíra'] },
        { nombre: 'Soria', localidades: ['Soria', 'Almazán', 'Olvega'] },
        { nombre: 'Tarragona', localidades: ['Tarragona', 'Reus', 'Cambrils'] },
        { nombre: 'Santa Cruz de Tenerife', localidades: ['Santa Cruz de Tenerife', 'San Cristóbal de La Laguna', 'Arona'] },
        { nombre: 'Teruel', localidades: ['Teruel', 'Alcañiz', 'Andorra'] },
        { nombre: 'Toledo', localidades: ['Toledo', 'Talavera de la Reina', 'Illescas'] },
        { nombre: 'Valencia', localidades: ['Valencia', 'Torrente', 'Paterna'] },
        { nombre: 'Valladolid', localidades: ['Valladolid', 'Medina del Campo', 'Laguna de Duero'] },
        { nombre: 'Vizcaya', localidades: ['Bilbao', 'Baracaldo', 'Getxo'] },
        { nombre: 'Zamora', localidades: ['Zamora', 'Benavente', 'Toro'] },
        { nombre: 'Zaragoza', localidades: ['Zaragoza', 'Calatayud', 'Utebo'] } ],
      mostrarProvincia: false,
      mostrarLocalidad: false,
    };
  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  methods: {
    validarNombre() {
      this.nombreValido = this.nombre.length > 0 && this.nombre.length <= 30;
    },
    validarTelefono() {
      this.telefonoValido = /^\d{9}$/.test(this.telefono);
    },
    validarLocalidad() {
      this.localidadValida = this.localidad.length > 0;
    },
    validarProvincia() {
      this.provinciaValida = this.provincia.length > 0;
    },
    validarCalle() {
      this.calleValida = this.calle.length > 0;
    },
    validarCodPostal() {
      this.codPostalValido = /^\d{5}$/.test(this.codPostal);
    },
    validarPais() {
      this.paisValido = this.pais.length > 0;
    },
    paisSeleccionado() {
      if (this.pais === 'España') {
        this.mostrarProvincia = true;
      } else {
        this.mostrarProvincia = false;
        this.mostrarLocalidad = false;
      }
    },
    provinciaSeleccionada() {
      if (this.provincia) {
        this.mostrarLocalidad = true;
      } else {
        this.mostrarLocalidad = false;
      }
    },
    validarFormulario() {
      if (!this.pais) {
        alert('Por favor, seleccione un país.');
        return false;
      }

      // Validar que se haya seleccionado la provincia si es necesario
      if (this.mostrarProvincia && !this.provincia) {
        alert('Por favor, seleccione una provincia.');
        return false;
      }

      // Validar que se haya seleccionado la localidad si es necesario
      if (this.mostrarLocalidad && !this.localidad) {
        alert('Por favor, seleccione una localidad.');
        return false;
      }
      this.validarNombre();
      this.validarTelefono();
      this.validarLocalidad();
      this.validarProvincia();
      this.validarCalle();
      this.validarCodPostal();
      this.validarPais();

      return this.nombreValido && this.telefonoValido && this.localidadValida &&
          this.provinciaValida && this.calleValida && this.codPostalValido && this.paisValido;
    },
    async agregarEstablecimiento() {
      if (!this.validarFormulario()) {
        alert('Error en la validación del formulario');
        return;
      }
      console.log(this.username);
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

        this.nombre = '';
        this.telefono = null;
        this.localidad = '';
        this.provincia = '';
        this.calle = '';
        this.codPostal = null;
        this.pais = '';
        this.mostrarProvincia = false;
        this.mostrarLocalidad = false;
        this.provincias = null;

        location.reload();

      } catch (error) {
        console.error('Error al agregar establecimiento:', error);
      }
    }
  }
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
  width: 70vw;
  margin: 50px auto 100px;
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
}

h1 {
  font-size: 32px;
  font-weight: bold;
  color: #000000;
  text-align: center;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 30px;
}

.form-control {
  border: 2px solid #ddd;
  border-radius: 30px;
  padding: 15px;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
  width: 100%;
}

.form-control:focus {
  border-color: #9DD9D2;
  box-shadow: 0px 0px 10px 0px rgba(157, 217, 210, 0.5);
}

label {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.btn-primary {
  background-color: #9DD9D2 !important;
  border: none;
  border-color: transparent !important;
  color: #fff;
  padding: 15px 30px;
  border-radius: 30px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.3s ease;
  display: inline-block;
  text-align: center;
  text-decoration: none;
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
}

.btn-primary:hover {
  background-color: #7F7F7F;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.2);
  transform: scale(1.05);
}

.form-control:hover {
  box-shadow: 0px 0px 10px 0px rgba(0,0,0,0.1);
}
</style>

