<template>
  <div class="container">
    <h1 class="text-center">¡Pasa a formar parte de Ocio Sin Gluten!</h1>
    <p class="subtitle text-center">¡Introduzca sus datos para pasar a formar parte de la comunidad celiaca!</p>
    <form @submit.prevent="agregarUsuario" class="row">
      <!-- Columna izquierda -->
      <div class="col-md-4">
        <div class="form-group">
          <label for="dni">DNI:</label>
          <input type="text" class="form-control" id="dni" v-model="nuevoUsuario.dni" required>
        </div>
        <div class="form-group">
          <label for="username">Nombre de Usuario:</label>
          <input type="text" class="form-control" id="username" v-model="nuevoUsuario.username" required>
        </div>
        <div class="form-group">
          <label for="nombre">Nombre:</label>
          <input type="text" class="form-control" id="nombre" v-model="nuevoUsuario.nombre" required>
        </div>
        <div class="form-group">
          <label for="apellidos">Apellidos:</label>
          <input type="text" class="form-control" id="apellidos" v-model="nuevoUsuario.apellidos" required>
        </div>
        <div class="form-group">
          <label for="fechaNacimiento">Fecha de Nacimiento:</label>
          <input type="date" class="form-control" id="fechaNacimiento" v-model="nuevoUsuario.fechaNacimiento" required>
        </div>
      </div>
      <!-- Columna central -->
      <div class="col-md-4">
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" class="form-control" id="email" v-model="nuevoUsuario.email" required>
        </div>
        <div class="form-group">
          <label for="telefono">Teléfono:</label>
          <input type="text" class="form-control" id="telefono" v-model="nuevoUsuario.telefono" required>
        </div>
        <div class="form-group">
          <label for="password">Contraseña:</label>
          <input type="password" class="form-control" id="password" v-model="nuevoUsuario.password" required>
        </div>
        <div class="form-group">
          <label for="confirmPassword">Confirmar Contraseña:</label>
          <input type="password" class="form-control" id="confirmPassword" v-model="confirmPassword" required>
        </div>
      </div>
      <!-- Columna derecha -->
      <div class="col-md-4">
        <!-- Campo de carga de foto de perfil -->
        <div class="form-group">
          <label for="fotoPerfil">Foto de Perfil:</label>
          <input type="file" class="form-control-file" id="fotoPerfil" @change="onFileChange">
        </div>
        <!-- Botón de Agregar Usuario -->
        <div class="form-group">
          <button type="submit" class="btn btn-primary">Agregar Usuario</button>
        </div>
      </div>
    </form>
  </div>
</template>

<script>
import UsuarioServicio from "../services/UsuarioServicio";

export default {
  name: 'AgregarUsuario',
  data() {
    return {
      nuevoUsuario: {
        dni: '',
        username: '',
        nombre: '',
        apellidos: '',
        fechaNacimiento: '',
        telefono: '',
        fotoPerfil: null,
        email: '',
        password: ''
      },
      confirmPassword: '' // Variable para confirmar la contraseña
    }
  },
  methods: {
    agregarUsuario() {
      if (this.nuevoUsuario.password !== this.confirmPassword) {
        alert('Las contraseñas no coinciden');
        return;
      }

      const formData = new FormData();
      formData.append('dni', this.nuevoUsuario.dni);
      formData.append('username', this.nuevoUsuario.username);
      formData.append('nombre', this.nuevoUsuario.nombre);
      formData.append('apellidos', this.nuevoUsuario.apellidos);
      formData.append('fechaNacimiento', this.nuevoUsuario.fechaNacimiento);
      formData.append('telefono', this.nuevoUsuario.telefono);
      formData.append('fotoPerfil', this.nuevoUsuario.fotoPerfil);
      formData.append('email', this.nuevoUsuario.email);
      formData.append('password', this.nuevoUsuario.password);

      UsuarioServicio.agregarUsuario(formData)
          .then(() => {
            // Limpiar el formulario después de agregar el usuario
            this.nuevoUsuario = {
              dni: '',
              username: '',
              nombre: '',
              apellidos: '',
              fechaNacimiento: '',
              telefono: '',
              fotoPerfil: null,
              email: '',
              password: ''
            };
            // Actualizar la lista de usuarios después de agregar uno nuevo
            this.$emit('usuario-agregado');
          })
          .catch(error => {
            console.error('Error al agregar usuario:', error);
          });
    },
    onFileChange(event) {
      const selectedFile = event.target.files[0];
      const reader = new FileReader();

      reader.onload = (e) => {
        // Convertir la imagen a un arreglo de bytes (byte[])
        const imageData = new Uint8Array(e.target.result);

        // Asignar los bytes de la imagen al objeto nuevoUsuario
        this.nuevoUsuario.fotoPerfil = imageData;
      };

      // Leer el contenido de la imagen como un arreglo de bytes
      reader.readAsArrayBuffer(selectedFile);
    }

  }
}
</script>



<style>
@import '../assets/css/registro.css';
</style>
