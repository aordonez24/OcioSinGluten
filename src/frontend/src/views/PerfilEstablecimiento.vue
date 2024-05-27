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
      <p>
        <i v-if="token" class="far fa-thumbs-up like-icon" @click="likeEstablecimiento(establecimiento.idEstablecimiento)">
          {{ establecimiento.numLikes}}
        </i>
        <span v-else>
          Debes iniciar sesión para dar like
        </span>
      </p>
      <button @click="esFavorito ? quitarComoFav() : marcarComoFavorito()" class="boton-subir">
        {{ esFavorito ? 'Quitar como favorito' : 'Marcar como favorito' }}
      </button>
      <button @click="marcarComoVisitado" class="boton-subir" :disabled="esVisitado">
        {{ esVisitado ? 'Ya has visitado este establecimiento' : 'Marcar como visitado' }}
      </button>
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
  <div class="comentarios">
    <div class="titulo">
      <h2> Comentarios </h2>
    </div>
    <div v-if="token" class="nuevo-comentario">
      <div class="campo-comentario-con-boton">
        <textarea v-model="nuevoComentario" placeholder="Introduce tu comentario (máximo 140 caracteres)" maxlength="140" class="campo-comentario"></textarea>
        <button @click="enviarComentario" class="boton-subir">Enviar</button>
      </div>
    </div>
    <div v-for="(comentario, index) in comentarios" :key="index" class="comentario">
      <div class="comentario-contenido">
        <p><strong>{{comentario.autor.username}}</strong> Comentó: {{comentario.mensaje}}</p>
        <p>{{comentario.fecha}}</p>
        <div class="acciones">
          <i class="fas fa-thumbs-up fa-lg" @click="darLike(comentario.id)">{{ comentario.numLikes}}</i> <!-- Icono de pulgar hacia arriba -->
          <i class="fas fa-comment fa-lg" @click="toggleRespuesta(index)">{{ comentario.comentarios.length}} </i> <!-- Icono de comentario -->
          <!-- Icono de papelera para borrar el comentario -->
          <i v-if="comentario.autor.username === usuarioActual" class="fas fa-trash-alt fa-lg" @click="eliminarComentario(comentario.id)"></i>
        </div>
        <div v-if="respuestasAbiertas.includes(index)" class="nueva-respuesta">
          <div class="campo-comentario-con-boton">
            <textarea v-model="nuevaRespuesta[index]" placeholder="Introduce tu respuesta (máximo 140 caracteres)" maxlength="140" class="campo-comentario"></textarea>
            <button @click="enviarRespuesta(index, comentario.id)" class="boton-subir">Enviar</button>
          </div>
        </div>
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
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
  margin: auto; /* Centra horizontal y verticalmente */
  margin-top: 20px; /* Ajusta el margen superior según necesites */

}

.comentarios {
  margin: auto; /* Centra horizontal y verticalmente */
  display: ruby-base; /* Utilizar flexbox para distribuir los elementos */
  justify-content: center; /* Espacio entre los elementos */
  width: 75vw; /* Ancho del viewport */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0,0,0,0.1);
  border: 1px solid #ccc;
  margin-top: 20px;
  margin-bottom: 20px;
}

.comentario {
  margin-bottom: 20px;
}

.comentario-contenido {
  border: 1px solid #ccc; /* Borde alrededor del contenido */
  border-radius: 5px; /* Bordes redondeados */
  padding: 10px; /* Espaciado interno */
}

.acciones {
  margin-top: 10px;
}

.acciones i {
  margin-right: 10px; /* Espacio entre los iconos */
  cursor: pointer; /* Cambia el cursor al pasar sobre los iconos */
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
  margin-left: 30px;
  margin-right: 20px;
}

.nuevo-comentario {
  margin-bottom: 20px;
}

.campo-comentario-con-boton {
  display: flex;
  align-items: center;
}

.campo-comentario {
  flex: 1;
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
  margin-right: 10px;
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
      map: null,
      comentarios: [],
      usuarioActual: '',
      nuevoComentario: '',
      sePuedeResponder: false,
      respuestasAbiertas: [], // Arreglo para almacenar los índices de los comentarios con el campo de respuesta abierto
      nuevaRespuesta: {}, // Objeto para almacenar las respuestas escritas por el usuario para cada comentario
      favoritosUsuario: [],
      visitadosUsuario: [],
      esFavorito: false, // Indica si el establecimiento es favorito del usuario
      esVisitado: false // Indica si el establecimiento ha sido visitado por el usuario
    };
  },
  mounted() {
    this.obtenerDatosEstablecimiento();
    this.$nextTick(() => {
      this.mostrarMapa();
    });
    this.cargarComentarios();
    this.usuarioActual = localStorage.getItem('username');
    console.log(this.usuarioActual);
    this.obtenerFavoritosUsuario();
    this.obtenerVisitadosUsuario();
  },
  methods: {
    async obtenerDatosEstablecimiento() {
      // Obtener datos del establecimiento
      const id = this.$route.params.idEstablecimiento;
      const response = await axios.get(`http://localhost:8080/ociosingluten/establecimientos/establecimiento/${id}`);
      this.establecimiento = response.data;
      this.imagenes = this.establecimiento.imagenesBase64.map(base64 => 'data:image/png;base64,' + base64);
    },
    async obtenerFavoritosUsuario() {
      const username = localStorage.getItem('username');
      const response1 = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/estFavoritos`);
      this.favoritosUsuario = response1.data;
      const id = this.$route.params.idEstablecimiento;
      for (let i = 0; i < this.favoritosUsuario.length; i++) {
        if(parseInt(this.favoritosUsuario[i].idEstablecimiento) === parseInt(id)){
          this.esFavorito = true;
          break;
        }
      }
    },
    async obtenerVisitadosUsuario() {
      const username = localStorage.getItem('username');
      const response2 = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/estVisitados`);
      this.visitadosUsuario = response2.data;
      const id = this.$route.params.idEstablecimiento;
      for (let i = 0; i < this.visitadosUsuario.length; i++) {
        if(parseInt(this.visitadosUsuario[i].idEstablecimiento) === parseInt(id)){
          this.esVisitado = true;
          break;
        }
      }
    },
    openFileInput() {
      if (this.token) {
        this.$refs.fileInput.click();
      } else {
        this.$router.push('/iniciaSesion'); // Redirige al usuario a la página de inicio de sesión si no ha iniciado sesión
      }
    },
    enviarComentario() {
      // Verifica si el comentario no está vacío
      if (this.nuevoComentario.trim() === '') {
        return;
      }
      // Envía el comentario al backend utilizando axios
      const id = this.$route.params.idEstablecimiento;
      const nuevoComentarioDTO = {
        username: this.usuarioActual,
        mensaje: this.nuevoComentario
      };
      console.log(nuevoComentarioDTO);
      axios.post(`http://localhost:8080/ociosingluten/establecimientos/establecimientos/${id}/nuevoComentario`, nuevoComentarioDTO)
          .then(() => {
            // Si el comentario se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
            // Limpia el área de texto después de enviar el comentario
            this.nuevoComentario = '';
          })
          .catch(error => {
            console.error('Error al enviar el comentario:', error);
          });
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
          return [latitud, longitud];
        } else {
          console.error('No se encontraron resultados para la dirección proporcionada.');
        }
      } catch (error) {
        console.error('Error al obtener coordenadas:', error);
      }
    },
    async cargarComentarios() {
      try {
        const id = this.$route.params.idEstablecimiento;
        const response = await axios.get(`http://localhost:8080/ociosingluten/establecimientos/establecimientos/${id}/comentarios`);
        this.comentarios = response.data;
        console.log(this.comentarios);
      } catch (error) {
        console.error('Error al cargar los comentarios:', error);
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
    },
    async darLike(id){
      axios.post(`http://localhost:8080/ociosingluten/comentario/${id}/nuevoLike`)
          .then(() => {
            // Si el comentario se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
          })
          .catch(error => {
            console.error('Error al enviar el comentario:', error);
          });
    },
    async marcarComoFavorito(){
      const id = this.$route.params.idEstablecimiento;
      const username = localStorage.getItem('username');
      axios.post(`http://localhost:8080/ociosingluten/establecimientos/${id}/favorito`, username)
          .then(() => {
            // Si el comentario se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
            this.esFavorito = true; // Actualiza el estado a favorito
          })
          .catch(error => {
            console.error('Error al enviar el comentario:', error);
          });
    },
    async marcarComoVisitado(){
      const id = this.$route.params.idEstablecimiento;
      const username = localStorage.getItem('username');
      axios.post(`http://localhost:8080/ociosingluten/establecimientos/${id}/visitado`, username)
          .then(() => {
            // Si el comentario se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
            this.esVisitado = true; // Actualiza el estado a visitado
          })
          .catch(error => {
            console.error('Error al enviar el comentario:', error);
          });
    },
    async quitarComoFav() {
      try {
        const id = this.$route.params.idEstablecimiento;
        const username = localStorage.getItem('username');
        const response = await axios.delete(`http://localhost:8080/ociosingluten/establecimientos/${id}/nofavorito`, {
          params: {
            username: username
          }
        });
        console.log(response);
        if (response.status === 200) {
          this.esFavorito = false;
          console.log('Establecimiento quitado como favorito con éxito.');
        } else {
          console.error('Error al quitar el establecimiento como favorito:', response.data);
        }
      } catch (error) {
        console.error('Error al quitar el establecimiento como favorito:', error);
      }
    },
    toggleRespuesta(index) {
      // Verifica si el comentario ya tiene el campo de respuesta abierto
      const indiceRespuesta = this.respuestasAbiertas.indexOf(index);
      if (indiceRespuesta === -1) {
        // Si no está abierto, lo agregamos al arreglo de respuestas abiertas
        this.respuestasAbiertas.push(index);
        // Inicializamos la respuesta para ese comentario
        this.nuevaRespuesta[index] = '';
      } else {
        // Si está abierto, lo eliminamos del arreglo de respuestas abiertas
        this.respuestasAbiertas.splice(indiceRespuesta, 1);
        // También eliminamos la respuesta asociada
        delete this.nuevaRespuesta[index];
      }
    },
    async enviarRespuesta(index, id) {
      // Verifica si la respuesta no está vacía
      const respuesta = this.nuevaRespuesta[index];
      if (respuesta.trim() === '') {
        return;
      }
      // Envía la respuesta al backend utilizando axios
      const nuevoComentarioDTO = {
        username: localStorage.getItem('username'),
        mensaje: respuesta
      };
      console.log(nuevoComentarioDTO);
      console.log(id);
      axios.post(`http://localhost:8080/ociosingluten/comentario/${id}/nuevaRespuesta`, nuevoComentarioDTO)
          .then(() => {
            // Si la respuesta se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
            // Limpia el área de texto después de enviar la respuesta
            this.nuevaRespuesta[index] = '';
            // Cierra el campo de respuesta
            this.toggleRespuesta(index);
          })
          .catch(error => {
            console.error('Error al enviar la respuesta:', error);
          });
    },
    async eliminarComentario(id) {
      const username = localStorage.getItem('username');
      const idEstablecimiento = this.$route.params.idEstablecimiento;
      const url = `http://localhost:8080/ociosingluten/comentario/${id}?username=${username}&idEstablecimiento=${idEstablecimiento}`;

      axios.delete(url)
          .then(() => {
            // Si la respuesta se envía correctamente, actualiza la lista de comentarios
            this.cargarComentarios();
          })
          .catch(error => {
            console.error('Error al enviar la solicitud:', error);
          });
    },
    async likeEstablecimiento(id) {
      axios.post(`http://localhost:8080/ociosingluten/establecimientos/${id}/nuevoLike`, localStorage.getItem('username'))
          .then(() => {
            location.reload();
          })
          .catch(error => {
            console.error('Error al enviar el like:', error);
          });

    }

  }
}
</script>

