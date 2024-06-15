<template>
  <div class="page-container">
    <header3 />
    <div class="container1">
      <div class="container-principal">
        <h1>Quejas</h1>
        <p class="intro-text">
          Si desea enviar una queja, complete el formulario y haga clic en enviar.
        </p>
        <div v-for="queja in quejas" :key="queja.idQueja" class="queja">
          <div class="contenido-queja">
            <div>
              <strong class="nombre-usuario">{{ queja.nombreUsuario }} ({{ queja.email }})</strong> comentó: {{ queja.mensaje }}
            </div>
            <button class="boton-subir" @click="responderQueja(queja.email, queja.mensaje)">Responder</button>
            <button class="boton-subir" @click="marcarComoRespondida(queja.idQueja)">Marcar como respondida</button>
          </div>
        </div>
      </div>
    </div>
    <contacto />
    <footer-componente />
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import Header3 from "@/components/headerIniciadoSesion.vue";
import FooterComponente from "@/components/footer.vue";
import { mapGetters } from "vuex";
import contacto from "@/components/contacto.vue";

export default {
  components: { Header3, FooterComponente, contacto },
  computed: {
    ...mapGetters(['username', 'isAuthenticated'])
  },
  setup() {
    const quejas = ref([]);
    const mensajeEnviado = ref(false);
    const namelon = ref('');
    const email = ref('');
    const message = ref('');

    const fetchActividades = async () => {
      try {
        const response = await axios.get('http://localhost:8080/ociosingluten/quejas/listadoQuejas');
        quejas.value = response.data;
      } catch (error) {
        console.error('Error fetching actividades:', error);
      }
    };

    const handleSubmit = () => {
      if (!validateForm()) {
        return;
      }
      axios.post('http://localhost:8080/ociosingluten/quejas/nuevaQueja', {
        nombre: namelon.value,
        email: email.value,
        mensaje: message.value
      })
          .then(response => {
            console.log('Mensaje enviado con éxito:', response.data);
            mensajeEnviado.value = true;
          })
          .catch(error => {
            console.error('Error al enviar el mensaje:', error);
          });
    };

    const validateForm = () => {
      if (!namelon.value || !email.value || !message.value) {
        alert('Todos los campos son obligatorios.');
        return false;
      }
      return true;
    };

    const responderQueja = (email, mensaje) => {
      const subject = 'Respuesta de Ocio Sin Gluten';
      const body = `En respuesta a tu comentario "${mensaje}", Ocio sin gluten le hace saber que:`;
      window.location.href = `mailto:${email}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
    };

    const marcarComoRespondida = async (idQueja) => {
      try {
        await axios.delete(`http://localhost:8080/ociosingluten/quejas/quitaQueja/${idQueja}`);
        location.reload();
      } catch (error) {
        console.error('Error al eliminar la queja:', error);
      }
    };

    onMounted(() => {
      fetchActividades();
    });

    return {
      quejas,
      handleSubmit,
      mensajeEnviado,
      namelon,
      email,
      message,
      responderQueja,
      marcarComoRespondida
    };
  }
};
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.container-principal {
  flex: 1;
  min-height: calc(80vh - 200px);
  width: 90%;
  margin: 40px auto;
  padding: 30px;
  background-image: linear-gradient(120deg, #ffffff 0%, #ffcc74 100%);
  border-radius: 20px;
  box-shadow: 0px 0px 20px 0px rgba(0, 0, 0, 0.1);
  border: 1px solid #000000;
}

.intro-text {
  max-width: 600px;
  margin: 0 auto 20px;
  text-align: center;
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

.container1 {
  background-image: url("@/assets/images/_01d90abf-9b74-4813-b728-42c7b8f918a7.jpg");
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-repeat: no-repeat;
  background-size: cover;
  padding: 20px;
}
</style>
