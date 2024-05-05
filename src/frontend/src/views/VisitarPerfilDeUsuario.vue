<template>
  <div>
    <header3/>
    <div class="container-principal">
      <div class="profile-container">
        <div class="left-column">
          <div class="profile-picture-container">
            <div v-if="fotoPerfilURL" class="picture-button-container">
              <img
                  :src="'data:image/jpeg;base64,' + fotoPerfilURL"
                  alt="Foto de perfil"
                  class="profile-picture"
              />
            </div>
            <div v-else class="profile-picture-default">
              <i class="fas fa-user"></i>
            </div>
            <div class="seguidores-seguidos">
              <div class="seguidor-seguido">
                <p class="pulsable" @click="verSeguidores">Seguidores</p>
                <span>{{ numSeguidores }}</span>
              </div>
              <div class="seguidor-seguido">
                <p class="pulsable" @click="verSeguidos">Seguidos</p>
                <span>{{ numSeguidos }}</span>
              </div>
            </div>
          </div>

          <!-- Botón de chat -->
          <button @click="iniciarChat" class="cerrar-sesion-button">Chat con usuario</button>

          <!-- Botón de seguir usuario -->
          <button @click="toggleSeguir" class="cerrar-sesion-button">{{ siguiendo ? 'Dejar de seguir' : 'Seguir usuario' }}</button>


        </div>
        <div class="right-column">
          <div class="datosDeUsuario">
            <p><strong>Nombre de usuario:</strong>
              <span v-if="!editing"> {{ username }}</span>
              <input v-else v-model="editedUsername" type="text">
            </p>
            <p><strong>Nombre:</strong>
              <span v-if="!editing"> {{ nombre }}</span>
              <input v-else v-model="editedNombre" type="text">
            </p>
            <p><strong>Apellidos:</strong>
              <span v-if="!editing"> {{ apellidos }}</span>
              <input v-else v-model="editedApellidos" type="text">
            </p>
          </div>

        </div>
      </div>
    </div>
  </div>
  <div class="extra-space">
    <footer-componente/>
  </div>
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
      fotoPerfilURL: null,
      dni: '',
      username: '',
      nombre: '',
      apellidos: '',
      fechaNacimiento: '',
      telefono: '',
      email: '',
      numSeguidos: '',
      numSeguidores: '',
      siguiendo: false, // Nuevo dato para controlar si se está siguiendo al usuario
    };
  },
  mounted() {
    this.obtenerUsuario();
    this.obtenerSeguidores();
    this.obtenerSeguidos();
    this.checkSeguido();
  },
  methods: {
    async obtenerUsuario() {
      try {
        this.loading = true;
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${username}`);
        this.usuario = response.data;
        this.fotoPerfilURL = response.data.fotoPerfil;
        this.dni = response.data.dni;
        this.username = response.data.username;
        this.email = response.data.email;
        this.fechaNacimiento = response.data.fechaNacimiento;
        this.nombre = response.data.nombre;
        this.apellidos = response.data.apellidos;
        this.telefono = response.data.telefono;
        console.log(this.usuario);
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    },
    async obtenerSeguidores(){
      const username = this.$route.params.username;
      const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidores`);
      this.numSeguidores = response.data.length;
    },
    async obtenerSeguidos(){
      const username = this.$route.params.username;
      const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidos`);
      this.numSeguidos = response.data.length;
    },
    verSeguidores() {
      const username = this.$route.params.username;
      this.$router.push({ name: 'SeguidosSeguidores', params: { username: username } });
    },
    verSeguidos() {
      const username = this.$route.params.username;
      this.$router.push({ name: 'SeguidosSeguidores', params: { username: username } });
    },
    iniciarChat() {
      const username = this.$route.params.username; // Nombre de usuario del perfil visitado
      this.$router.push({ name: 'chatCon', params: { username: username } }); // Navegar al chat con ese usuario
    },
    async toggleSeguir() {
      try {
        const username = this.$route.params.username;
        const loggedInUsername = localStorage.getItem('username');

        // Verificar si el usuario intenta seguirse a sí mismo
        if (username === loggedInUsername) {
          console.error('No puedes seguirte a ti mismo.');
          return; // Salir del método sin hacer nada más
        }

        if (this.siguiendo) {
          // Dejar de seguir
          await axios.delete(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidorMenos`, {
            data: {
              usernameQueSigueA: loggedInUsername,
              usernameAQuienSigue: username
            }
          });
        } else {
          // Seguir usuario
          await axios.post(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/nuevoSeguidor`, {
            usernameQueSigueA: loggedInUsername,
            usernameAQuienSigue: username
          });
        }
        window.location.reload();

        // Actualizar estado de seguimiento
        this.siguiendo = !this.siguiendo;
      } catch (error) {
        console.error('Error al seguir/dejar de seguir al usuario:', error);
      }
    },
    async checkSeguido() {
      try {
        const buscado = this.$route.params.username;
        const username = localStorage.getItem('username');
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidos`);
        for (let i = 0; i < response.data.length; i++) {
          if(buscado === response.data[i].username){
            this.siguiendo = true; //Lo está siguiendo
          }
        }
        } catch (error) {
        console.error('Error al verificar si se sigue al usuario:', error);
      }
    }
  }
};
</script>

<style scoped>
.container-principal {
  min-height: calc(70vh - 200px); /* Ajusta este valor según la altura de tu encabezado y pie de página */
  width: 70vw;
  margin: 50px auto 0; /* Eliminamos el margen inferior */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  display: flex;
  flex-direction: column; /* Asegura que el pie de página se coloque al final */
}

.profile-container {
  display: flex;
  justify-content: space-between;
}

.left-column {
  flex: 1;
  margin-right: 20px;
}

.right-column {
  flex: 2;
}

.profile-picture-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.profile-picture {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 20px;
}

.seguidores-seguidos {
  margin-left: 75px;
  display: flex;
  justify-content: space-between;
}

.seguidor-seguido {
  text-align: center;
  margin-bottom: 120px; /* Agrega un margen inferior */
}

.seguidor-seguido:first-child {
  margin-right: 20px;
}

.profile-picture-default {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background-color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px; /* Tamaño del icono */
}

.seguidor-seguido p {
  margin-bottom: 20px;
}

.seguidor-seguido span {
  font-weight: bold;
}

.pulsable {
  cursor: pointer;
  font-size: 20px;
}


.cerrar-sesion-button {
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
}

.datosDeUsuario {
  margin-left: 75px;
  background-color: #9DD9D2;
  padding: 20px;
  border-radius: 10px;
  color: white;
}

.extra-space {
  margin-top: 210px; /* Ajusta este valor según sea necesario */
}

.cerrar-sesion-button2 {
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: white;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}


.change-password-form label {
  display: block;
  margin-bottom: 5px;
}

.change-password-form input {
  margin-bottom: 10px;
  padding: 5px;
  width: 100%;
}

.change-password-form button {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.change-password-form button:hover {
  background-color: #ffcc74;
}


.change-password-button3 {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.change-password-button3:hover {
  background-color: #ffcc74;
}

footer-componente {
  margin-top: auto; /* Empuja el pie de página al final de la página */
}

</style>
