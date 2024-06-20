<template>
  <div>
    <header3/>
    <div class="container1">
      <div class="container-principal">
      <div class="profile-container">
        <div class="left-column">
          <div class="profile-picture-container">
            <div class="picture-button-container">
              <img
                  v-if="fotoPerfilURL"
                  :src="'data:image/jpeg;base64,' + fotoPerfilURL"
                  alt="Foto de perfil"
                  class="profile-picture"
              />
              <input type="file" ref="fileInput" style="display: none" @change="onFileChange">
              <button @click="openFileInput" class="button">Subir foto de perfil</button>
              <button @click="eliminarFoto" class="button">Eliminar foto de perfil</button>
              <button @click="iniciarVisitadosYFavs" class="button">Establecimientos visitados y favoritos</button>
              <button @click="cerrarSesion" class="button">Cerrar sesión</button>
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
              <p><strong>DNI:</strong> {{ dni }}</p>
              <p><strong>Nombre de usuario:</strong>{{ }} {{ username }}</p>
              <p><strong>Nombre:</strong>
                <span v-if="!editing">{{ }} {{ nombre }}</span>
                <input v-else v-model="editedNombre" type="text" class="editing-input">
              </p>
              <p><strong>Apellidos:</strong>
                <span v-if="!editing">{{ }} {{ apellidos }}</span>
                <input v-else v-model="editedApellidos" type="text" class="editing-input">
              </p>
              <p><strong>Fecha de nacimiento:</strong>
                <span v-if="!editing">{{ }} {{ fechaNacimiento }}</span>
                <input v-else v-model="editedFechaNacimiento" type="date" class="editing-input">
              </p>
              <p><strong>Teléfono:</strong>
                <span v-if="!editing">{{ }} {{ telefono }}</span>
                <input v-else v-model="editedTelefono" type="text" class="editing-input">
              </p>
              <p><strong>Email:</strong>{{ }} {{ emailon }}</p>

              <div v-if="!changingPassword" class="button-group">
                <button v-if="!editing" @click="editing = true" class="cerrar-sesion-button2">Editar</button>
                <div class="button-separator"></div>
                <button @click="iniciarCambioContrasena" class="cerrar-sesion-button2">Cambiar Contraseña</button>
                <button v-if="editing" @click="guardarCambios" class="cerrar-sesion-button2">Guardar</button>
              </div>

              <!-- Formulario para cambiar contraseña -->
              <div v-if="changingPassword" class="change-password-form">
                <h3>Cambiar Contraseña</h3>
                <form @submit.prevent="submitCambiarContrasena">
                  <div>
                    <label for="newPassword" class="white-label">Nueva Contraseña:</label>
                    <input id="newPassword" type="password" v-model="newPassword" class="white-input" required>
                  </div>
                  <div>
                    <label for="confirmPassword" class="white-label">Confirmar Contraseña:</label>
                    <input id="confirmPassword" type="password" v-model="confirmPassword" class="white-input" required>
                  </div>
                  <button type="submit" class="change-password-button3">Guardar Contraseña</button>
                  <button @click="cancelarCambioContrasena" class="change-password-button3">Cancelar</button>
                </form>
              </div>
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
import contacto from "@/components/contacto.vue";
import { mapActions } from 'vuex';

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
      username: '',
      nombre: '',
      apellidos: '',
      fechaNacimiento: '',
      telefono: '',
      emailon: '',
      editing: false,
      editedUsername: '',
      editedNombre: '',
      editedApellidos: '',
      editedFechaNacimiento: '',
      editedTelefono: '',
      changingPassword: false,
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
      numSeguidos: '',
      numSeguidores: '',
    };
  },
  mounted() {
    this.obtenerUsuario();
    this.obtenerSeguidores();
    this.obtenerSeguidos();
  },
  methods: {
    ...mapActions(['logout']), // Mapear la acción logout del store Vuex
    async cerrarSesion() {
      try {
        await this.logout(); // Llamar a la acción logout del store Vuex
        this.$router.push('/'); // Redirigir a la página de inicio
      } catch (error) {
        console.error('Error al cerrar sesión:', error);
      }
    },
    async obtenerUsuario() {
      try {
        this.loading = true;
        const username = this.$route.params.username;
        const response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${username}`);
        this.usuario = response.data;
        this.fotoPerfilURL = response.data.fotoPerfil;
        this.dni = response.data.dni;
        this.username = response.data.username;
        this.emailon = response.data.email;
        this.fechaNacimiento = response.data.fechaNacimiento;
        this.nombre = response.data.nombre;
        this.apellidos = response.data.apellidos;
        this.telefono = response.data.telefono;
        this.editedUsername = this.username;
        this.editedNombre = this.nombre;
        this.editedApellidos = this.apellidos;
        this.editedFechaNacimiento = this.fechaNacimiento;
        this.editedTelefono=this.telefono;
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    },
    openFileInput() {
      this.$refs.fileInput.click();
    },
    async eliminarFoto() {
      try {
        const username = this.username;
        const response = await axios.delete(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/quitafotoPerfil`);
        if (response.status === 200) {
          // Actualizar la vista después de eliminar la foto de perfil
          this.fotoPerfilURL = null;
          alert("Foto de perfil eliminada exitosamente");
        } else {
          alert("Error al eliminar la foto de perfil");
        }
      } catch (error) {
        console.error('Error al eliminar la foto de perfil:', error);
        alert("Error al eliminar la foto de perfil");
      }
    },
    async onFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = async (e) => {
          const fotoPerfilBase64 = e.target.result.split(',')[1];
          try {
            const username = this.$route.params.username;
            await axios.post(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/fotoPerfil`, fotoPerfilBase64, {
              headers: {
                'Content-Type': 'text/plain' // Establecer el tipo de contenido como texto plano
              }
            });
            // Recargar la página después de actualizar la foto de perfil
            this.obtenerUsuario(); // Otra opción es solo actualizar la foto de perfil sin recargar la página
          } catch (error) {
            console.error('Error al actualizar la foto de perfil:', error);
          }
        };
        reader.readAsDataURL(file);
      }
    },
    async guardarCambios() {
      try {
        const username = this.$route.params.username;
        const nuevosDatos = {
          username: this.editedUsername,
          nombre: this.editedNombre,
          apellidos: this.editedApellidos,
          fechaNacimiento: this.editedFechaNacimiento,
          telefono: this.editedTelefono
        };

        if (!this.validarTelefono(nuevosDatos.telefono)) {
          alert('El número de teléfono no es válido.');
          return;
        }

        if (!this.validarEdad(nuevosDatos.fechaNacimiento)) {
          alert('Debe ser mayor de edad.');
          return;
        }

        // Si pasa la validación, realizar la solicitud de actualización
        await axios.put(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/nuevosDatos`, nuevosDatos);
        this.editing = false; // Cambiar al modo de visualización después de guardar los cambios
        this.$router.push('/iniciaSesion');

      } catch (error) {
        console.error('Error al guardar los cambios:', error);
        alert("Error al guardar los cambios. Por favor, inténtalo de nuevo más tarde.");
      }
    },
    iniciarVisitadosYFavs() {
      const username = this.$route.params.username; // Nombre de usuario del perfil visitado
      this.$router.push({ name: 'FavoritosYVisitados', params: { username: username } }); // Navegar al chat con ese usuario
    },
    async iniciarCambioContrasena() { //Async por operacion asincrona
      this.changingPassword = true;
    },
    cancelarCambioContrasena() {
      // Limpiar los campos y volver al estado anterior
      this.changingPassword = false;
      this.currentPassword = '';
      this.newPassword = '';
      this.confirmPassword = '';
    },
    async submitCambiarContrasena() {
      try {

          // Verificar que la nueva contraseña y la confirmación coincidan
          if (this.newPassword !== this.confirmPassword) {
            alert("Las contraseñas no coinciden.");
            return;
          }

          const nuevaContrasena = this.newPassword;

          const username = this.$route.params.username;
          const data = {username, nuevaContrasena};

          const response = await axios.put(
              `http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/nuevaPassword`,
              data
          );

          if (response.status === 200) {
            alert("¡Contraseña cambiada exitosamente!");
            this.cancelarCambioContrasena();
            this.$router.push('/iniciaSesion');
          } else {
            alert("Error al cambiar la contraseña");
          }
      } catch (error) {
        console.error('Error al cambiar la contraseña:', error);
        alert("Error al cambiar la contraseña");
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
    validarTelefono(telefono) {
      return /^\d{9}$/.test(telefono);
    },
    validarEdad(fechaNacimiento) {
      const fechaActual = new Date();
      const fechaNac = new Date(fechaNacimiento);
      const diferencia = fechaActual - fechaNac;
      const edad = new Date(diferencia).getUTCFullYear() - 1970;
      return edad >= 18;
    },
    handleSubmit() {
      console.log("hola");
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
            console.error('Error al enviar el mensaje:', error);
          });
    },
    validateForm() {
      if (!this.name || !this.email || !this.message) {
        alert('Todos los campos son obligatorios.');
        return false;
      }
    },
  }
};
</script>

<style scoped>
.container-principal {
  width: 70vw;
  margin: 50px auto 0; /* Eliminamos el margen inferior */
  padding: 45px;
  background-color: #fff;
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #ccc;
  margin-bottom: 50px;
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

.datosDeUsuario {
  margin-left: 75px;
  background-color: #9DD9D2;
  padding: 20px;
  border-radius: 10px;
  color: white;
  width: 80%; /* Cambia el ancho del div de los datos del usuario */
  margin-top: 20px; /* Ajusta el margen superior según sea necesario */
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

.cerrar-sesion-button2:hover {
  background-color: #ffcc74;
}

.button-group {
  display: flex;
  align-items: center;
}

.button-separator {
  width: 10px; /* Ajusta este valor para la separación deseada */
}

.change-password-form {
  margin-top: 20px;
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

.white-label {
  color: white;
}

.white-input {
  background-color: white;
  color: black;
  border: 1px solid #ccc;
  padding: 5px;
  border-radius: 5px;
}

.change-password-button3 {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: white;
  background-color: white;
  border: none;
  cursor: pointer;
}

.editing-input {
  background-color: #fff;
  border: 1px solid #ccc;
  padding: 5px;
  border-radius: 5px;
  width: 100%;
}

.change-password-button3:hover {
  background-color: #ffcc74;
}

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat; /* Evitar que la imagen se repita */
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
