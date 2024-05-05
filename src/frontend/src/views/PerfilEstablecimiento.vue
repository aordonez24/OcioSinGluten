<template>
  <template v-if="token">
    <header3/>
  </template>
  <template v-else>
    <cabecera-componente/>
  </template>
  <div class="container-principal">
    <div class="datos">
      <!-- Mostrar los datos del establecimiento -->
      <h1>{{ establecimiento.nombre }}</h1>
      <p>Teléfono: {{ establecimiento.telefono }}</p>
      <p>Localidad: {{ establecimiento.localidad }}</p>
      <p>Provincia: {{ establecimiento.provincia }}</p>
      <p>Calle: {{ establecimiento.calle }}</p>
      <p>Código Postal: {{ establecimiento.codPostal }}</p>
      <p>País: {{ establecimiento.pais }}</p>
      <p>Número de Likes: {{ establecimiento.numLikes }}</p>
    </div>
    <div class="mapa" ref="map"></div>
    <div class="imagenes">
      <!-- Galería de imágenes -->
      <div class="galeria">
        <img v-for="(imagen, index) in imagenes" :key="index" :src="imagen" alt="Imagen" @click="mostrarImagen(index)">
      </div>
      <!-- Botón para subir imágenes -->
      <input type="file" ref="fileInput" style="display: none" @change="onFileChange">
      <button @click="openFileInput" class="boton-subir">¿Dispone de alguna imagen de este establecimiento? ¡Compártela con nosotros!</button>
    </div>
    <!-- Modal para mostrar la imagen en tamaño completo -->
    <div class="modal" v-if="imagenSeleccionada !== null">
      <div class="modal-contenido">
        <span class="cerrar" @click="cerrarModal">&times;</span>
        <img :src="imagenSeleccionada" alt="Imagen" style="max-width: 100%; max-height: 100%;">
      </div>
    </div>

  </div>
  <footer-componente/>
</template>

<style scoped>
.container-principal {
  display: flex; /* Utilizar flexbox para distribuir los elementos */
  justify-content: space-between; /* Espacio entre los elementos */
  width: 75vw; /* Ancho del viewport */
  margin: 50px auto 200px; /* Centra el contenedor horizontalmente y deja un margen vertical de 50px arriba y 100px abajo */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
}

.datos {
  width: 45%; /* Ancho del contenedor de datos */
}

.imagenes {
  width: 45%; /* Ancho del contenedor de imágenes */
  display: flex; /* Utilizar flexbox para distribuir los elementos */
  flex-direction: column; /* Distribución en columna */
  align-items: flex-start; /* Alineación a la izquierda */
}

.boton-subir {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.boton-subir:hover {
  background-color: #ffcc74;
}

.galeria {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); /* Cambiar el tamaño mínimo y máximo de las columnas */
  grid-gap: 10px;
}

.galeria img {
  max-width: 100%;
  max-height: 150px; /* Cambiar la altura máxima de las imágenes */
  width: auto; /* Para mantener la proporción */
  cursor: pointer;
}

/* Estilos para el modal */
.modal {
  display: block;
  position: fixed;
  z-index: 1;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  overflow: auto;
}

.modal-contenido {
  background-color: #fefefe;
  margin: 15% auto;
  padding: 20px;
  border: 1px solid #888;
  width: 80%;
  max-width: 800px; /* Limita el ancho máximo */
  text-align: center;
}

.cerrar {
  color: #aaa;
  float: right;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
}

.cerrar:hover,
.cerrar:focus {
  color: black;
  text-decoration: none;
  cursor: pointer;
}

/* Estilo del contenedor del mapa */
.mapa {
  width: 100%;
  height: 50vh; /* Set a relative height, adjust as needed */
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

/* Estilo para ocultar la atribución de Esri */
.esri-attribution {
  display: none !important; /* Oculta la atribución */
}

</style>


<script>
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import CabeceraComponente from "@/components/header.vue";
import axios from "axios";
import 'ol/ol.css'; // Importa los estilos CSS de OpenLayers
import { loadModules } from 'esri-loader';


export default {
  name: 'Vista-Establecimiento',
  components: {
    CabeceraComponente,
    FooterComponente,
    Header3
  },
  data() {
    return {
      token: localStorage.getItem('token'),
      establecimiento: '',
      imagenes: [],
      imagenSeleccionada: null,
      map: null
    };
  },
  mounted() {
    this.obtenerDatosEstablecimiento();
    this.$nextTick(() => {
      this.mostrarMapa();
    });
  },
  methods: {
    async obtenerDatosEstablecimiento() {
      // Obtener datos del establecimiento
      const id = this.$route.params.idEstablecimiento;
      const response = await axios.get(`http://localhost:8080/ociosingluten/establecimientos/establecimiento/${id}`);
      this.establecimiento = response.data;
      this.imagenes = this.establecimiento.imagenesBase64.map(base64 => 'data:image/png;base64,' + base64);
    },
    openFileInput() {
      this.$refs.fileInput.click();
    },
    async onFileChange(event) {
      const file = event.target.files[0];
      const id = this.$route.params.idEstablecimiento;
      if (file) {
        const reader = new FileReader();
        reader.onload = async (e) => {
          const fotoPerfilBase64 = e.target.result.split(',')[1];
          try {
            await axios.post(`http://localhost:8080/ociosingluten/establecimientos/establecimientoFoto/${id}/nuevaFoto`, fotoPerfilBase64, {
              headers: {
                'Content-Type': 'text/plain'
              }
            });
            await this.obtenerDatosEstablecimiento();
          } catch (error) {
            console.error('Error al actualizar la foto de perfil:', error);
          }
        };
        reader.readAsDataURL(file);
      }
    },
    mostrarImagen(index) {
      this.imagenSeleccionada = this.imagenes[index];
    },
    cerrarModal() {
      this.imagenSeleccionada = null;
    },
    async obtenerCoordenadas() {
      try {
        const id = this.$route.params.idEstablecimiento;
        const response1 = await axios.get(`http://localhost:8080/ociosingluten/establecimientos/establecimiento/${id}`);
        this.establecimiento = response1.data;
        const direccion = `${this.establecimiento.calle}, ${this.establecimiento.localidad}, ${this.establecimiento.provincia}, ${this.establecimiento.codPostal}, ${this.establecimiento.pais}`;
        const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(direccion)}&format=json`;

        const response = await axios.get(url);
        if (response.data && response.data.length > 0) {
          const resultado = response.data[0];
          const latitud = parseFloat(resultado.lat);
          const longitud = parseFloat(resultado.lon);
          console.log('Latitud:', latitud);
          console.log('Longitud:', longitud);
          return [latitud, longitud];
        } else {
          console.error('No se encontraron resultados para la dirección proporcionada.');
        }
      } catch (error) {
        console.error('Error al obtener coordenadas:', error);
      }
    },
    async mostrarMapa() {
      try {
        const coordenadas = await this.obtenerCoordenadas();

        // Carga los módulos necesarios de ArcGIS API
        const [Map, MapView, Graphic, Point] = await loadModules([
          'esri/Map',
          'esri/views/MapView',
          'esri/Graphic',
          'esri/geometry/Point'
        ]);

        // Crea una instancia del mapa
        const map = new Map({
          basemap: 'streets-navigation-vector' // Puedes cambiar el basemap según tus preferencias
        });

        // Crea una instancia de MapView y asigna el mapa
        const view = new MapView({
          container: this.$refs.map, // Debes asegurarte de que este elemento exista en tu template y tenga una referencia 'map'
          map,
          center: [coordenadas[1], coordenadas[0]], // Invierte las coordenadas para ArcGIS API
          zoom: 14, // Puedes ajustar el nivel de zoom según tus preferencias
          ui: {
            components: [] // Solo incluye el componente de zoom
          }
        });

        // Desactiva la atribución
        view.attributionVisible = false;

        // Crea un punto gráfico para la ubicación
        const point = new Point({
          longitude: coordenadas[1],
          latitude: coordenadas[0]
        });

        // Crea un gráfico para mostrar el punto en el mapa
        const graphic = new Graphic({
          geometry: point,
          symbol: {
            type: 'simple-marker',
            color: 'yellow', // Puedes ajustar el color y el tamaño del marcador
            size: '12px'
          }
        });

        // Añade el gráfico al mapa
        view.graphics.add(graphic);
      } catch (error) {
        console.error('Error al mostrar el mapa:', error);
      }
    }

  }
}
</script>
