<template>
  <template v-if="isAuthenticated">
    <header3/>
  </template>
  <template v-else>
    <cabecera-componente/>
  </template>
  <div class="container1">
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
          <i v-if="isAuthenticated" class="far fa-thumbs-up like-icon" @click="likeEstablecimiento(establecimiento.idEstablecimiento)">
            {{ establecimiento.numLikes}}
          </i>
          <span v-else>
            <strong>Debe iniciar sesión para dar like y marcar este establecimiento como favorito y/o visitado.</strong>
          </span>
        </p>
        <button v-if="isAuthenticated && !esFavorito" @click="marcarComoFavorito()" class="boton-subir">
          Marcar como favorito
        </button>
        <button v-if="isAuthenticated && esFavorito" @click="quitarComoFav()" class="boton-subir">
          Quitar como favorito
        </button>

        <button v-if="isAuthenticated && !esVisitado" @click="marcarComoVisitado()" class="boton-subir">
          Marcar como visitado
        </button>
        <button v-if="isAuthenticated && esVisitado" @click="eliminarVis()" class="boton-subir">
          Quitar como visitado
        </button>

        <button v-if="rol === 'ADMIN'" @click="toggleEditarEstablecimiento" class="boton-subir2">Editar establecimiento</button> <!-- Botón de edición para administradores -->
        <button v-if="rol === 'ADMIN'" @click="eliminarEstablecimiento" class="boton-subir2">Eliminar establecimiento</button> <!-- Botón de eliminación para administradores -->

        <div v-if="mostrarFormularioEditar" class="formulario-edicion">
          <h2>Editar Establecimiento</h2>
          <form @submit.prevent="editarEstablecimiento">
            <div>
              <label for="nombre">Nombre del Establecimiento:</label>
              <input type="text" v-model="formulario.nombre" id="nombre" required>
            </div>
            <div>
              <label for="telefono">Teléfono:</label>
              <input type="text" v-model="formulario.telefono" id="telefono" required>
            </div>
            <div>
              <label for="localidad">Localidad:</label>
              <input type="text" v-model="formulario.localidad" id="localidad" required>
            </div>
            <div>
              <label for="provincia">Provincia:</label>
              <input type="text" v-model="formulario.provincia" id="provincia" required>
            </div>
            <div>
              <label for="calle">Calle:</label>
              <input type="text" v-model="formulario.calle" id="calle" required>
            </div>
            <div>
              <label for="codPostal">Código Postal:</label>
              <input type="text" v-model="formulario.codPostal" id="codPostal" required>
            </div>
            <div>
              <label for="pais">País:</label>
              <input type="text" v-model="formulario.pais" id="pais" required>
            </div>
            <button type="submit" class="boton-subir">Guardar cambios</button>
            <button type="button" @click="cancelarEdicion" class="boton-subir2">Cancelar</button>
          </form>
        </div>
      </div>
      <div class="mapa" ref="map"></div>
      <div class="imagenes">
        <!-- Galería de imágenes -->
        <div class="galeria">
          <img v-for="(imagen, index) in imagenes" :key="index" :src="imagen" alt="Imagen" @click="mostrarImagen(index)">
        </div>
        <!-- Botón para subir imágenes -->
        <input type="file" ref="fileInput" style="display: none" @change="onFileChange">
        <button v-if="isAuthenticated" @click="openFileInput" class="boton-subir">¿Dispone de alguna imagen de este establecimiento? ¡Compártela con nosotros!</button>
        <p>
          Valoración media:
          <span v-for="(estrella, index) in calcularEstrellas()" :key="index">
            <i v-if="estrella === 'full'" class="fas fa-star star-yellow star-size"></i>
            <i v-if="estrella === 'half'" class="fas fa-star-half-alt star-yellow star-size"></i>
            <i v-if="estrella === 'empty'" class="far fa-star star-yellow star-size"></i>
          </span>
          (Calculada como la media entre el número de likes y los usuarios que han visitado este establecimiento.)
        </p>


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
      <div v-if="isAuthenticated" class="nuevo-comentario">
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
  </div>
  <contacto/>
  <footer-componente/>
</template>

<script>
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import CabeceraComponente from "@/components/header.vue";
import axios from "axios";
import 'ol/ol.css'; // Importa los estilos CSS de OpenLayers
import { loadModules } from 'esri-loader';
import {mapGetters} from "vuex";
import contacto from "@/components/contacto.vue";

export default {
  name: 'Vista-Establecimiento',
  components: {
    CabeceraComponente,
    FooterComponente,
    Header3,
    contacto
  },
  data() {
    return {
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
      esVisitado: false, // Indica si el establecimiento ha sido visitado por el usuario
      rol: '',
      mostrarFormularioEditar: false, // Estado para controlar la visibilidad del formulario de edición
      formulario: {
        nombre:'',
        telefono: '',
        localidad: '',
        provincia: '',
        calle: '',
        codPostal: '',
        pais: ''
      },
      valoracionMedia:'',
      mensajeEnviado: false

    };
  },
  mounted() {
    this.obtenerDatosEstablecimiento();
    this.$nextTick(() => {
      this.mostrarMapa();
    });
    this.cargarComentarios();
    this.usuarioActual = this.username;
    this.obtenerFavoritosUsuario();
    this.obtenerVisitadosUsuario();
    this.obtenerDatosUsuario();

  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  methods: {
    handleSubmit() {
      if(!this.validateForm()){
        return;
      }
      axios.post('http://localhost:8080/ociosingluten/quejas/nuevaQueja', {
        nombre: this.name,
        email: this.email,
        mensaje: this.message
      })
          .then(response => {
            console.log('Mensaje enviado con éxito:', response.data);
            this.mensajeEnviado = true;
          })
          .catch(error => {
            // Manejar errores en caso de que la solicitud falle
            console.error('Error al enviar el mensaje:', error);
          });
    },
    validateForm() {
      if (!this.name || !this.email || !this.message) {
        alert('Todos los campos son obligatorios.');
        return false;
      }
    },
    async obtenerDatosEstablecimiento() {
      // Obtener datos del establecimiento
      const id = this.$route.params.idEstablecimiento;
      const response = await axios.get(`http://localhost:8080/ociosingluten/establecimientos/establecimiento/${id}`);
      this.establecimiento = response.data;
      this.imagenes = this.establecimiento.imagenesBase64.map(base64 => 'data:image/png;base64,' + base64);
      console.log(this.establecimiento);
      if(this.establecimiento.numLikes < this.establecimiento.visitantes.length) {
        this.valoracionMedia = 5 * (this.establecimiento.numLikes / this.establecimiento.visitantes.length);
      }else{
        this.valoracionMedia = 5 * (this.establecimiento.visitantes.length / this.establecimiento.numLikes);
      }
    },
    async obtenerFavoritosUsuario() {
      if (!this.isAuthenticated) {
        return;
      }
      const username = this.username;
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
      if (!this.isAuthenticated) {
        return;
      }
      const username = this.username;
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
    async obtenerDatosUsuario(){
      if (!this.isAuthenticated) {
        return;
      }
      const yo = this.username;
      const response2 = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${yo}`);
      this.rol = response2.data.rol;
    },
    openFileInput() {
      if (this.isAuthenticated) {
        this.$refs.fileInput.click();
      } else {
        this.$router.push('/iniciaSesion'); // Redirige al usuario a la página de inicio de sesión si no ha iniciado sesión
      }
    },
    calcularEstrellas() {
        const valoracionMedia = this.valoracionMedia;
        const estrellasLlenas = Math.floor(valoracionMedia);
        const estrellaMedia = valoracionMedia - estrellasLlenas;
        let estrellas = [];

        for (let i = 0; i < estrellasLlenas; i++) {
          estrellas.push('full'); // Añadir estrellas llenas
        }

        if (estrellaMedia >= 0.5) {
          estrellas.push('half'); // Añadir media estrella si corresponde
        }

        const estrellasRestantes = 5 - estrellas.length;
        for (let i = 0; i < estrellasRestantes; i++) {
          estrellas.push('empty'); // Añadir estrellas vacías para completar el total
        }

        return estrellas;
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
    async marcarComoFavorito(){
      const id = this.$route.params.idEstablecimiento;
      const username = this.username;
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
      const username = this.username;
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
        const username = this.username;
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
    async eliminarVis() {
      try {
        const id = this.$route.params.idEstablecimiento;
        const username = this.username;
        const response = await axios.delete(`http://localhost:8080/ociosingluten/establecimientos/${id}/noVisitado`, {
          params: {
            username: username
          }
        });
        console.log(response);
        if (response.status === 200) {
          this.esVisitado = false;
          console.log('Establecimiento quitado como favorito con éxito.');
        } else {
          console.error('Error al quitar el establecimiento como favorito:', response.data);
        }
      } catch (error) {
        console.error('Error al quitar el establecimiento como favorito:', error);
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
        username: this.username,
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
      const username = this.username;
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
      try {
        const response = await axios.post(`http://localhost:8080/ociosingluten/establecimientos/${id}/nuevoLike`, this.username);
        if (response.status === 202) {
          this.quitarLike(id);
        } else if (response.status === 200) {
          location.reload();
        }
      } catch (error) {
        alert('Error al enviar el like:', error);
      }
    },
    async quitarLike(id) {
      try {
        const username = this.username;
        const response = await axios.post(`http://localhost:8080/ociosingluten/establecimientos/${id}/eliminaLike`, username);
        if (response.status === 200) {
          // Si se quita el like correctamente, actualiza la lista de comentarios u otra acción necesaria
          console.log('Like quitado con éxito.');
          // Aquí puedes agregar cualquier otra acción que desees realizar después de quitar el like.
          location.reload(); // Recarga la página para reflejar los cambios
        } else {
          console.error('Error al quitar el like:', response.data);
        }
      } catch (error) {
        console.error('Error al quitar el like:', error);
      }
    },
    toggleEditarEstablecimiento() {
      this.formulario.nombre = this.establecimiento.nombre;
      this.formulario.telefono = this.establecimiento.telefono;
      this.formulario.localidad = this.establecimiento.localidad;
      this.formulario.provincia = this.establecimiento.provincia;
      this.formulario.calle = this.establecimiento.calle;
      this.formulario.codPostal = this.establecimiento.codPostal;
      this.formulario.pais = this.establecimiento.pais;
      this.mostrarFormularioEditar = !this.mostrarFormularioEditar;
    },
    cancelarEdicion() {
      this.mostrarFormularioEditar = !this.mostrarFormularioEditar;
    },
    async editarEstablecimiento() {
      // Validar los campos antes de enviar la solicitud
      if (!this.validarFormulario()) {
        console.error('Error en la validación del formulario');
        return;
      }

      const id = this.$route.params.idEstablecimiento;
      const requestData = {
        nombre: this.formulario.nombre,
        telefono: this.formulario.telefono,
        localidad: this.formulario.localidad,
        provincia: this.formulario.provincia,
        calle: this.formulario.calle,
        codPostal: this.formulario.codPostal,
        pais: this.formulario.pais
      };

      try {
        const response = await axios.put(
            `http://localhost:8080/ociosingluten/establecimientos/establecimientos/${id}/modEstablecimiento`,
            requestData,
            {
              headers: {
                'Content-Type': 'application/json'
              }
            }
        );
        if (response.status === 200) {
          this.establecimiento = response.data;
          this.mostrarFormularioEditar = false;
          this.obtenerDatosEstablecimiento();
        }
      } catch (error) {
        console.error('Error al editar el establecimiento:', error);
      }
    },
    validarFormulario() {
      // Realizar la validación de los campos
      if (this.formulario.nombre.length > 0 && this.formulario.nombre.length <= 30) {
        this.nombreValido = this.formulario.nombre.length > 0 && this.formulario.nombre.length <= 30;
      }else{
        alert("Nombre incorrecto");
      }

      if (/^\d{9}$/.test(this.formulario.telefono)) {
        this.telefonoValido = /^\d{9}$/.test(this.formulario.telefono);
      }else{
        alert("Telefono incorrecto");
      }

      if(this.formulario.localidad.length > 0) {
        this.localidadValida = this.formulario.localidad.length > 0;
      }else{
        alert("Localidad incorrecta");
      }

      if(this.formulario.provincia.length > 0) {
        this.provinciaValida = this.formulario.provincia.length > 0;
      }else{
        alert("Provincia incorrecta");
      }

      if(this.formulario.calle.length > 0) {
        this.calleValida = this.formulario.calle.length > 0;
      }else{
        alert("Calle incorrecta");
      }

      if(/^\d+$/.test(this.formulario.codPostal)) {
        this.codPostalValido = /^\d+$/.test(this.formulario.codPostal);
      }else{
        alert("Codigo postal incorrecto");
      }

      if(this.formulario.pais.length > 0) {
        this.paisValido = this.formulario.pais.length > 0;
      }else{
        alert("Pais incorrecto");
      }

      // Verificar si todos los campos son válidos
      return (
          this.nombreValido &&
          this.telefonoValido &&
          this.localidadValida &&
          this.provinciaValida &&
          this.calleValida &&
          this.codPostalValido &&
          this.paisValido
      );
    },
    eliminarEstablecimiento() {
      if (confirm("¿Desea confirmar la eliminación del establecimiento?")) {
        // Llama al método para eliminar el establecimiento si el usuario confirma
        this.confirmarEliminarEstablecimiento();
      } else {
        console.log("Eliminación cancelada");
      }
    },

    // Método para llamar al endpoint de eliminación del establecimiento
    confirmarEliminarEstablecimiento() {
      const id = this.$route.params.idEstablecimiento;
      axios.delete(`http://localhost:8080/ociosingluten/establecimientos/establecimientos/${id}`)
          .then(() => {
            // Si la eliminación es exitosa, redirige a otra página o realiza alguna acción adicional si es necesario
            console.log("Establecimiento eliminado con éxito");
          })
          .catch(error => {
            console.error("Error al eliminar el establecimiento:", error);
          });
      this.$router.push('/establecimientos'); // Redirige al usuario a la página de inicio de sesión si no ha iniciado sesión
    },

  }
}
</script>

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

.boton-subir2 {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.boton-subir2:hover {
  background-color: #ff0000;
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

.formulario-edicion {
  background-color: #f9f9f9;
  border: 1px solid #ccc;
  border-radius: 5px;
  padding: 20px;
  margin-top: 20px;
}

.formulario-edicion h2 {
  margin-bottom: 15px;
}

.formulario-edicion div {
  margin-bottom: 10px;
}

.formulario-edicion label {
  display: block;
  margin-bottom: 5px;
}

.formulario-edicion input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 5px;
}

.formulario-edicion button {
  padding: 10px 20px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.formulario-edicion button:hover {
  background-color: #ffcc74;
}

.star-yellow {
  color: #ffcc74;
}

.star-size {
  font-size: 20px; /* Cambia este valor al tamaño deseado */
}

.mensaje-enviado {
  display: flex;
  align-items: center; /* Centrar verticalmente */
  justify-content: center; /* Centrar horizontalmente */
  background-color: #353535; /* Mantener el fondo gris oscuro */
}

.mensaje-enviado h2 {
  color: white; /* Cambiar el color del texto a blanco */
  text-align: center; /* Asegurarse de que el texto esté centrado */
}

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat; /* Evitar que la imagen se repita */
  background-size: cover;
  padding: 20px;
}

</style>