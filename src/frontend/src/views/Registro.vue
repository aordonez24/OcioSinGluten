<template>
  <cabecera-componente2/>
  <div class="container2">
    <h1 class="text-center">¡Pasa a formar parte de Ocio Sin Gluten!</h1>
    <p class="subtitle text-center">¡Introduzca sus datos para pasar a formar parte de la comunidad celiaca!</p>
    <form @submit.prevent="agregarUsuario" class="row">
      <!-- Columna izquierda -->
      <div class="col-md-4">
        <div class="form-group">
          <label for="dni">DNI:</label>
          <input type="text" class="form-control" id="dni" v-model="nuevoUsuario.dni" required>
          <span v-if="!dniValido" class="text-danger">DNI no válido.</span>
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
          <input type="date" class="form-control" id="fechaNacimiento" v-model="nuevoUsuario.fechaNacimiento" @change="validarEdad" required>
          <span v-if="!mayorDeEdad" class="text-danger">Debes ser mayor de edad para registrarte.</span>
        </div>
      </div>
      <!-- Columna central -->
      <div class="col-md-4">
        <div class="form-group">
          <label for="email">Email:</label>
          <input type="email" class="form-control" id="email" v-model="nuevoUsuario.email" @input="validarEmail" required>
          <span v-if="!emailValido" class="text-danger">Email no válido.</span>
        </div>
        <div class="form-group">
          <label for="telefono">Teléfono:</label>
          <input type="text" class="form-control" id="telefono" v-model="nuevoUsuario.telefono" required>
          <span v-if="!telefonoValido" class="text-danger">Teléfono no válido.</span>
        </div>
        <div class="form-group position-relative">
          <label for="password">Contraseña:</label>
          <input
              :type="showPassword? 'text' : 'password'"
              class="form-control"
              id="password"
              v-model="nuevoUsuario.password"
              required
          >
          <button
              type="button"
              class="btn btn-secondary position-absolute end-0 top-50 translate-middle-y"
              style="width: 40px; height: 40px; padding: 0;"
              @click="showPassword =!showPassword"
          >
            <i :class="showPassword? 'fa fa-eye-slash' : 'fa fa-eye-slash'" aria-hidden="true"></i>
          </button>
          <span v-if="!passwordValida" class="text-danger">Contraseña incorrecta. Introduzca al menos 8 caracteres con una mayúscula.</span>
        </div>
        <div class="form-group position-relative">
          <label for="confirmPassword">Confirmar Contraseña:</label>
          <input
              :type="showConfirmPassword? 'text' : 'password'"
              class="form-control"
              id="confirmPassword"
              v-model="confirmPassword"
              required
          >
          <button
              type="button"
              class="btn btn-secondary position-absolute end-0 top-50 translate-middle-y"
              style="width: 40px; height: 40px; padding: 0;"
              @click="showConfirmPassword =!showConfirmPassword"
          >
            <i :class="showConfirmPassword? 'fa fa-eye-slash' : 'fa fa-eye-slash'" aria-hidden="true"></i>
          </button>
          <span v-if="!passwordsCoinciden" class="text-danger ">Las contraseñas no coinciden.</span>
        </div>
      </div>
      <!-- Columna derecha -->
      <div class="col-md-4">
        <!-- Campo de carga de foto de perfil -->
        <div class="form-group">
          <label for="fotoPerfil">Foto de Perfil:</label>
          <input type="file" class="form-control-file" id="fotoPerfil" @change="onFileChange">
          <!-- Previsualización de la imagen -->
          <div v-if="imagePreview" class="image-preview">
            <img :src="imagePreview" alt="Previsualización de la imagen" class="rounded-circle">
          </div>
          <!-- Mensaje de aviso -->
          <span v-if="showFormatWarning" class="text-danger">Por favor, seleccione una imagen en formato JPG o JPEG.</span>
        </div>
        <!-- Botón de Agregar Usuario -->
        <div class="form-group">
          <button type="submit" class="btn btn-primary">Agregar Usuario</button>
        </div>
      </div>
    </form>
  </div>
  <div class="extra-space">
    <footer-componente/>
  </div>
</template>

<script>
import UsuarioServicio from "../services/UsuarioServicio";
import CabeceraComponente2 from "@/components/header2.vue";
import FooterComponente from "@/components/footer.vue";

export default {
  name: 'AgregarUsuario',
  components: {FooterComponente, CabeceraComponente2},
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
      confirmPassword: '',
      imagePreview: '',
      showFormatWarning: false, // Mostrar advertencia de formato de imagen
      showPassword: false,
      showConfirmPassword: false,
      dniValido: false,
      emailValido: false,
      telefonoValido: false,
      passwordValida: false,
      passwordsCoinciden: false,
      mayorDeEdad: false
    }
  },
  methods: {
    agregarUsuario() {
      // Validaciones
      if (!this.dniValido) {
        alert('DNI no válido');
        return;
      }
      if (!this.emailValido) {
        alert('Email no válido');
        return;
      }
      if (!this.telefonoValido) {
        alert('Teléfono no válido');
        return;
      }
      if (!this.passwordValida) {
        alert('La contraseña no cumple con los requisitos');
        return;
      }
      if (!this.passwordsCoinciden) {
        alert('Las contraseñas no coinciden');
        return;
      }
      if (!this.mayorDeEdad) {
        alert('Debes ser mayor de edad para registrarte');
        return;
      }

      // Proceso de agregar usuario
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
            this.confirmPassword = '';
            // Actualizar la lista de usuarios después de agregar uno nuevo
            this.$emit('usuario-agregado');
          })
          .catch(error => {
            console.error('Error al agregar usuario:', error);
          });
    },
    validarEmail() {
      this.emailValido = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.nuevoUsuario.email);
    },
    validarEdad() {
      const fechaNacimiento = new Date(this.nuevoUsuario.fechaNacimiento);
      const edadMinima = 18; // Edad mínima para registrarse

      const hoy = new Date();
      let edad = hoy.getFullYear() - fechaNacimiento.getFullYear();
      const mes = hoy.getMonth() - fechaNacimiento.getMonth();

      if (mes < 0 || (mes === 0 && hoy.getDate() < fechaNacimiento.getDate())) {
        edad = edad - 1;
      }

      this.mayorDeEdad = edad >= edadMinima;
    },
    onFileChange(event) {
      const selectedFile = event.target.files[0];

      // Verificar el tipo de archivo seleccionado
      if (!['image/jpeg', 'image/jpg'].includes(selectedFile.type)) {
        this.showFormatWarning = true; // Mostrar advertencia si el tipo de archivo no es válido
        return;
      }
      this.showFormatWarning = false; // Ocultar advertencia si el tipo de archivo es válido

      const reader = new FileReader();

      reader.onload = (e) => {
        this.imagePreview = e.target.result; // URL base64 de la imagen para previsualización
        this.nuevoUsuario.fotoPerfil = e.target.result.split(',')[1]; // Base64 de la imagen
      };

      reader.readAsDataURL(selectedFile);
    }
  }
}
</script>

<style scoped>
@import '../assets/css/registro.css';
</style>