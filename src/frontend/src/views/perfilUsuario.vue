<template>
  <div>
    <header3/>
    <div class="container-principal">
      <div class="profile-container">
        <div class="left-column">
          <div class="profile-picture-container">
            <img
                v-if="fotoPerfilURL"
                :src="'data:image/jpeg;base64,' + fotoPerfilURL"
                alt="Foto de perfil"
                class="profile-picture"
            />
            <input type="file" ref="fileInput" style="display: none" @change="onFileChange">
            <button @click="openFileInput" class="change-profile-picture-button">Cambiar foto de perfil</button>
          </div>
          <button @click="cerrarSesion" class="cerrar-sesion-button">Cerrar sesión</button>
        </div>
        <div class="right-column">
          <div class="datosDeUsuario">
            <p><strong>DNI:</strong> {{ dni }}</p>
            <p><strong>Nombre de usuario:</strong>
              <span v-if="!editing">{{ username }}</span>
              <input v-else v-model="editedUsername" type="text">
            </p>
            <p><strong>Nombre:</strong>
              <span v-if="!editing">{{ nombre }}</span>
              <input v-else v-model="editedNombre" type="text">
            </p>
            <p><strong>Apellidos:</strong>
              <span v-if="!editing">{{ apellidos }}</span>
              <input v-else v-model="editedApellidos" type="text">
            </p>
            <p><strong>Fecha de nacimiento:</strong>
              <span v-if="!editing">{{ fechaNacimiento }}</span>
              <input v-else v-model="editedFechaNacimiento" type="date">
            </p>
            <p><strong>Teléfono:</strong>
              <span v-if="!editing">{{ telefono }}</span>
              <input v-else v-model="editedTelefono" type="text">
            </p>
            <p><strong>Email:</strong> {{ email }}</p>

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
                  <input id="newPassword" type="password" v-model="newPassword" class="white-input">
                </div>
                <div>
                  <label for="confirmPassword" class="white-label">Confirmar Contraseña:</label>
                  <input id="confirmPassword" type="password" v-model="confirmPassword" class="white-input">
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
      editing: false,
      editedUsername: '',
      editedNombre: '',
      editedApellidos: '',
      editedFechaNacimiento: '',
      editedTelefono: '',
      changingPassword: false,
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
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
        this.fotoPerfilURL = response.data.fotoPerfil;
        this.dni = response.data.dni;
        this.username = response.data.username;
        this.email = response.data.email;
        this.fechaNacimiento = response.data.fechaNacimiento;
        this.nombre = response.data.nombre;
        this.apellidos = response.data.apellidos;
        this.telefono = response.data.telefono;
        this.editedUsername = this.username;
        this.editedNombre = this.nombre;
        this.editedApellidos = this.apellidos;
        this.editedFechaNacimiento = this.fechaNacimiento;
        this.editedTelefono=this.telefono;
        console.log(this.usuario);
      } catch (error) {
        console.error('Error al obtener datos del usuario:', error);
        this.loading = false;
      }
    },
    async cerrarSesion() {
      try {
        localStorage.removeItem('token');
        this.$router.push('/');
      } catch (error) {
        console.error('Error al cerrar sesión:', error);
      }
    },
    openFileInput() {
      this.$refs.fileInput.click();
    },
    async onFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = async (e) => {
          const fotoPerfilBase64 = e.target.result.split(',')[1];
          try {
            const username = this.$route.params.username;
            // Realizar la solicitud POST al servidor para actualizar la foto de perfil
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
        await axios.put(`http://localhost:8080/ociosingluten/usuarios/perfilUsuario/${username}/nuevosDatos`, nuevosDatos);
        this.editing = false; // Cambia a modo de visualización después de guardar los cambios
        this.$router.push('/iniciaSesion');

      } catch (error) {
        console.error('Error al guardar los cambios:', error);
      }
    },
    async iniciarCambioContrasena() {
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
    }
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
}

.profile-picture {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
}

.change-profile-picture-button {
  display: block;
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.change-profile-picture-button:hover {
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

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
}

.datosDeUsuario {
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

.cerrar-sesion-button2:hover {
  background-color: #ffa500;
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
  background-color: #ffa500;
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
  background-color: #9DD9D2;
  border: none;
  cursor: pointer;
}

.change-password-button3:hover {
  background-color: #ffa500;
}
</style>
