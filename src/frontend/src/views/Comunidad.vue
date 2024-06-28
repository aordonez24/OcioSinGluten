<template>
  <header3/>
  <div class="container-principal">
    <h2 class="welcome-text">¡Bienvenido a la comunidad de Ocio Sin Gluten!</h2>
    <p class="subtext">Busque a conocidos y comparta experiencias con ellos</p>
    <button @click="iniciarChat" class="cerrar-sesion-button">Acceda a nuestro chat grupal!</button>
    <div class="barra-busqueda">
      <input type="text" class="search-input" v-model="searchQuery" placeholder="Buscar usuario..." @input="filterUsers">
    </div>
    <div class="user-grid">
      <div v-for="user in filteredUsers" :key="user.username" class="user-card">
        <div class="user-avatar">
          <img v-if="user.fotoPerfil && user.fotoPerfil.length > 4" :src="'data:image/jpeg;base64,' + user.fotoPerfil"  />
          <i v-else class="fas fa-user"></i>
        </div>
        <div class="user-info">
          <div>
            <p @click="irAPerfil(user.username)" class="username-link"><strong>{{ user.username }}</strong></p>
          </div>
          <div>{{ user.nombre }} {{ user.apellidos }}</div>
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
import contacto from "@/components/contacto.vue";
import axios from 'axios';
import {mapGetters} from "vuex";

export default {
  name: 'comunidad-ociosingluten',
  components: {FooterComponente, Header3, contacto},
  data() {
    return {
      users: [],
      filteredUsers: [],
      searchQuery: '',
      rol:''
    };
  },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  mounted() {
    this.obtenerUsuarios();
  },
  methods: {
    filterUsers() {
      const loggedInUsername = this.username;
      this.filteredUsers = this.users.filter(user =>
          user.username.toLowerCase().includes(this.searchQuery.toLowerCase()) && user.username !== loggedInUsername
      );
    },
    async obtenerUsuarios() {
      const yo = this.username;
      const response2 =  await axios.get(`http://localhost:8080/ociosingluten/usuarios/perfilUsuarioUsername/${yo}`);
      this.rol = response2.data.rol;
      let response = "";
      if (this.rol === 'ADMIN') {
        response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/listadoUsuariosadmin`);
      } else {
        response = await axios.get(`http://localhost:8080/ociosingluten/usuarios/listadoUsuarioscomun`);
      }
      this.users = response.data;
      this.filterUsers();
    },
    irAPerfil(username) {
      this.$router.push({ name: 'perfilOtroUsuario', params: { username: username } });
    },
    iniciarChat() {
      this.$router.push({ name: 'chatGrupal'}); // Navegar al chat con ese usuario
    },
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
  width: 100%;
  background-color: #ffcc74;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.welcome-text {
  font-size: 2rem;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.subtext {
  font-size: 1.5rem;
  text-align: center;
  margin-bottom: 20px;
}

.search-input {
  margin-bottom: 20px;
  width: 50%;
}

.user-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.user-avatar {
  width: 6.25rem;
  height: 6.25rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  background-color: #ccc;
  color: white;
  margin-bottom: 10px;
}

.username-link {
  cursor: pointer;
  color: black;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
}

.user-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0px 0px 10px 0px rgba(0, 0, 0, 0.1);
  margin: 10px;
  flex: 1 1 calc(33.333% - 20px);
}

.user-info {
  text-align: center;
  margin-bottom: 10px;
}

.cerrar-sesion-button {
  padding: 8px 12px;
  border-radius: 20px;
  margin-bottom: 20px;
  color: black;
  background-color: white;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.cerrar-sesion-button:hover {
  background-color: #9DD9D2;
}

.barra-busqueda {
  margin-top: 10px;
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.barra-busqueda input {
  width: 80%;
  max-width: 600px;
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
  box-sizing: border-box;
  text-align: center;
}

@media (max-width: 768px) {
  .welcome-text {
    font-size: 1.5rem;
  }

  .subtext {
    font-size: 1.25rem;
  }

  .user-card {
    flex: 1 1 calc(50% - 20px);
    margin: 10px 5px;
  }

  .user-avatar {
    width: 4rem;
    height: 4rem;
    font-size: 1rem;
    margin-bottom: 10px;
  }

  .barra-busqueda input {
    width: 90%;
  }
}

@media (max-width: 480px) {
  .user-card {
    flex: 1 1 100%;
    margin: 10px 0;
  }

  .barra-busqueda input {
    width: 95%;
  }
}

</style>
