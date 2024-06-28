<template>
  <div>
    <header3/>
    <div class="container1">
      <div class="container-principal">
      <div class="profile-container">
        <div class="left-column">
          <div class="profile-picture-container">
            <div v-if="hayFoto" class="picture-button-container">
              <img
                  :src="'data:image/jpeg;base64,' + fotoPerfilURL"
                  alt="Foto de perfil"
                  class="profile-picture"
              />
              <button @click="toggleSeguir" class="button">{{ siguiendo ? 'Dejar de seguir' : 'Seguir usuario' }}</button>
              <button @click="iniciarVisitadosYFavs" class="button">Establecimientos visitados y favoritos</button>
              <button v-if="rol === 'ADMIN'" @click="archivado ? restaurarUsuario() : suspenderUsuario()" class="button2">{{ archivado ? 'Restaurar usuario' : 'Suspender usuario' }}</button>
              <button v-if="rol === 'ADMIN'" @click="eliminarUsuario" class="button2">Eliminar usuario</button>
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
            <div class="datosDeUsuario">
              <p><strong>Nombre de usuario:</strong>
                <span v-if="!editing">{{ }} {{ usernameUsuario }}</span>
                <input v-else v-model="editedUsername" type="text">
              </p>
              <p><strong>Nombre:</strong>
                <span v-if="!editing">{{ }} {{ nombre }}</span>
                <input v-else v-model="editedNombre" type="text">
              </p>
              <p><strong>Apellidos:</strong>
                <span v-if="!editing">{{ }} {{ apellidos }}</span>
                <input v-else v-model="editedApellidos" type="text">
              </p>
            </div>
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
import axios from 'axios';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import { mapGetters } from "vuex";
import contacto from "@/components/contacto.vue";

export default {
  name: 'PerfilUsuario',
  components: {
    Header3,
    FooterComponente,
    contacto
  },
  data() {
    return {
      usuario: null,
      loading: false,
      fotoPerfilURL: null,
      dni: '',
      usernameUsuario: '',
      nombre: '',
      apellidos: '',
      fechaNacimiento: '',
      telefono: '',
      email: '',
      numSeguidos: '',
      numSeguidores: '',
      siguiendo: false,
      rol: '',
      archivado: '',
      hayFoto: false
    };
  },
  mounted() {
    this.obtenerUsuario();
    this.obtenerSeguidores();
    this.obtenerSeguidos();
    this.checkSeguido();
  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  methods: {
    async obtenerUsuario() {
      try {
        this.loading = true;
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${username}`);
        this.usuario = response.data;
        this.fotoPerfilURL = response.data.fotoPerfil;

        if(this.fotoPerfilURL.length > 4){
          this.hayFoto = true;
        }else{
          this.hayFoto = false;
        }
        this.dni = response.data.dni;
        this.usernameUsuario = response.data.username;
        this.email = response.data.email;
        this.fechaNacimiento = response.data.fechaNacimiento;
        this.nombre = response.data.nombre;
        this.apellidos = response.data.apellidos;
        this.telefono = response.data.telefono;
        this.archivado = response.data.archivado;
        const yo = this.username;
        const response2 = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${yo}`);
        this.rol = response2.data.rol;
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    },
    async obtenerSeguidores() {
      const username = this.$route.params.username;
      const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidores`);
      this.numSeguidores = response.data.length;
    },
    async obtenerSeguidos() {
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
    iniciarVisitadosYFavs() {
      const username = this.$route.params.username; // Nombre de usuario del perfil visitado
      this.$router.push({ name: 'FavoritosYVisitados', params: { username: username } }); // Navegar al chat con ese usuario
    },
    async toggleSeguir() {
      try {
        const username = this.$route.params.username;
        const loggedInUsername = this.username;
        console.log(username);
        console.log(loggedInUsername);
        if (username === loggedInUsername) {
          console.error('No puedes seguirte a ti mismo.');
          return;
        }

        if (this.siguiendo) {
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

        this.siguiendo = !this.siguiendo;
      } catch (error) {
        console.error('Error al seguir/dejar de seguir al usuario:', error);
      }
    },
    async checkSeguido() {
      try {
        const buscado = this.$route.params.username;
        const username = this.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/seguidos`);
        for (let i = 0; i < response.data.length; i++) {
          if (buscado === response.data[i].username) {
            this.siguiendo = true;
          }
        }
      } catch (error) {
        console.error('Error al verificar si se sigue al usuario:', error);
      }
    },
    async suspenderUsuario() {
      try {
        const username = this.$route.params.username;
        console.log(username);
        await axios.post(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/oculto`);

        const emailUsuario = this.email;
        const asunto = 'Tu cuenta ha sido suspendida temporalmente';
        const cuerpo = 'Hola,\n\nTu cuenta ha sido suspendida temporalmente por violar nuestras políticas durante 24 horas.\n\nAtentamente,\nOcio Sin Gluten';

        const mailtoLink = `mailto:${emailUsuario}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;

        window.location.href = mailtoLink;

        this.$router.push({ name: 'comunidad' });
      } catch (error) {
        console.error('Error al suspender el usuario:', error);
      }
    },
    async restaurarUsuario() {
      try {
        const username = this.$route.params.username;
        console.log(username);
        await axios.post(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/desOculto`);

        const emailUsuario = this.email; // Obtener el correo electrónico del usuario
        const asunto = 'Tu cuenta ha sido restaurada';
        const cuerpo = 'Hola,\n\nTu cuenta ha sido restaurada y ya puedes acceder nuevamente.\n\nAtentamente,\nOcio Sin Gluten';

        const mailtoLink = `mailto:${emailUsuario}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;

        window.location.href = mailtoLink;

        this.$router.push({ name: 'comunidad' });
      } catch (error) {
        console.error('Error al restaurar el usuario:', error);
      }
    },
    async eliminarUsuario() {
      try {
        const username = this.$route.params.username;
        console.log(username);
        await axios.delete(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/usuarioMenos`);

        const emailUsuario = this.email;
        const asunto = 'Tu cuenta ha sido eliminada';
        const cuerpo = 'Hola,\n\nTu cuenta ha sido eliminada permanentemente por violar nuestras políticas. Para recuperarla, regístrese de nuevo.\n\nAtentamente,\nOcio Sin Gluten';

        const mailtoLink = `mailto:${emailUsuario}?subject=${encodeURIComponent(asunto)}&body=${encodeURIComponent(cuerpo)}`;

        window.location.href = mailtoLink;

        this.$router.push({ name: 'comunidad' });
      } catch (error) {
        console.error('Error al eliminar el usuario:', error);
      }
    }

  }
};
</script>


<style scoped>
.container-principal {
  min-height: calc(70vh - 200px);
  width: 70vw;
  margin: 50px auto 0;
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  display: flex;
  flex-direction: column;
  margin-bottom: 40px;
}

.button {
  display: block;
  width: 100%;
  max-width: 200px;
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.button:hover {
  background-color: #9DD9D2;
}

.button2 {
  display: block;
  width: 100%;
  max-width: 200px;
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.button2:hover {
  background-color: #ff0000;
}

.profile-container {
  display: flex;
  justify-content: space-between;
}

.left-column {
  flex: 1;
  margin-right: 20px;
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
  margin-left: 50px;
  display: flex;
  justify-content: space-between;
}

.seguidor-seguido {
  text-align: center;
  margin-bottom: 120px;
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
  font-size: 60px;
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

.cerrar-sesion-button {
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cerrar-sesion-button2:hover {
  background-color: #ff0000;
}

.datosDeUsuario {
  margin-left: 50px;
  background-color: #9DD9D2;
  padding: 20px;
  border-radius: 10px;
  color: white;
  width: 80%;
  margin-top: 20px;
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


footer-componente {
  margin-top: auto;
}

.action-buttons-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.profile-picture-default {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background-color: #ccc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 60px;
  color: #666;
}

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat;
  background-size: cover;
  padding: 20px;
}

@media screen and (max-width: 768px) {
  .container-principal {
    width: 95%;
    padding: 5px;
    flex-direction: column;
    align-items: center;
  }

  .left-column {
    flex: none;
    width: 100%;
    margin-right: 0;
  }

  .profile-picture-container {
    flex-direction: column;
    align-items: center;
  }

  .profile-picture {
    width: 100px;
    height: 100px;
    margin-right: 0;
  }

  .seguidores-seguidos {
    margin-top: 20px;
    margin-left: 0;
  }

  .datosDeUsuario {
    margin-top: -100px;
    flex-direction: column;
    align-items: center;
    margin-left: 0;
    width: 100%;
  }
}

</style>
