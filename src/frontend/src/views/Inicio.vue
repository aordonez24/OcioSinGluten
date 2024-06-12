<template>
  <div>
    <template v-if="isAuthenticated">
      <header3/>
    </template>
    <template v-else>
      <cabecera-componente/>
    </template>
    <div>
      <section class="main-section">
        <h2>¿Qué es la celiaquía?</h2>
        <p>La enfermedad celíaca es una afección en la que el cuerpo no puede tolerar el gluten. El gluten es una proteína que se encuentra en el trigo, la cebada y el centeno. Cuando las personas con celiaquía consumen gluten, su sistema inmunológico ataca el revestimiento del intestino delgado, lo que puede causar una serie de síntomas y problemas de salud.</p>
        <h3 v-if="!isAuthenticated">¡Inicia sesión o crea una cuenta nueva para pertenecer a la comunidad de Ocio Sin Gluten!</h3>
      </section>
      <div class="symptoms-container">
        <h3>Síntomas de la celiaquía</h3>
        <div class="symptoms-columns">
          <div class="column-container">
            <div class="left-column">
              <ul>
                <li>Diarrea crónica</li>
                <li>Distensión abdominal</li>
                <li>Pérdida de peso inexplicable</li>
                <li>Fatiga</li>
                <li>Erupciones cutáneas</li>
                <li>Dolor abdominal</li>
                <li>Anemia</li>
                <li>Dolor en las articulaciones</li>
              </ul>
            </div>
          </div>

        </div>
      </div>
    </div>
    <contacto/>
    <footer-componente/>
  </div>
</template>

<script>
import '@fortawesome/fontawesome-free/css/all.css';
import CabeceraComponente from "@/components/header.vue";
import FooterComponente from "@/components/footer.vue";
import contacto from "@/components/contacto.vue";
import Header3 from "@/components/headerIniciadoSesion.vue";
import axios from "axios";
import { mapGetters } from 'vuex';

export default {
  name: 'Vista-Inicio',
  components: {
    Header3,
    FooterComponente,
    CabeceraComponente,
    contacto
  },
  data() {
    return {
      name: '',
      email: '',
      message: '',
      mensajeEnviado: false
    };
  },
  computed: {
    ...mapGetters(['isAuthenticated'])
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
  }
}
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */
}

.main-section {
  background-color: #353535; /* Color de fondo gris */
  padding: 80px; /* Aumentar el padding para hacer la sección más grande */
  color: rgb(244, 255, 253); /* Color de texto blanco */
  text-align: center; /* Centrar texto */

}

.main-section h2 {
  color: rgb(244, 255, 253); /* Color de texto blanco */
  text-align: center; /* Centrar el título de los síntomas */
  margin-bottom: 20px; /* Agregar margen inferior al título */
  font-size: 45px; /* Tamaño de fuente más grande */
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */

}

.main-section h3 {
  color: #ffcc74; /* Color de texto blanco */
  text-align: center; /* Centrar el título de los síntomas */
  margin-top: 40px;
  font-size: 30px; /* Tamaño de fuente más grande */
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */
}

.main-section p {
  font-size: 26px; /* Tamaño de fuente más pequeño */
  line-height: 1.4; /* Espaciado entre líneas más compacto */
  max-width: 1000px; /* Ancho máximo del texto para evitar que se extienda demasiado */
  margin: 0 auto; /* Centrar el texto horizontalmente */
}

.symptoms-container {
  background-color: #ffffff; /* Color de fondo gris */
  margin-top: 0; /* Espacio entre el section y los síntomas */
  padding: 40px; /* Aumentar el padding para hacer la sección más grande */
}

.symptoms-container h3 {
  margin-top: -15px; /* Reducir margen superior del título */
  text-align: center; /* Centrar el título de los síntomas */
  margin-bottom: 30px; /* Agregar margen inferior al título */
  font-family: 'Montserrat', sans-serif; /* Aplicar la fuente Montserrat */
  font-size: 45px; /* Tamaño de fuente más grande */
}

.symptoms-columns {
  display: flex;
  justify-content: center; /* Centrar las columnas horizontalmente */
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px; /* Agregar un espacio entre las columnas */
}


.column-container {
  width: 20%; /* Reducir el ancho de los contenedores para que estén más juntos */
  margin: 0 20px; /* Agregar un margen entre las columnas */
  text-align: center; /* Centrar el contenido de los contenedores */
}

.left-column ul,
.right-column ul {
  padding-left: -10px;
  list-style-type: none; /* Eliminar los puntos de la lista */
  text-align: left; /* Alinear el texto de la lista a la izquierda */
}
.left-column ul li,
.right-column ul li {
  list-style-type: none;
  font-size: 22px; /* Cambia el tamaño de fuente de los síntomas */
  white-space: nowrap; /* Evitar el salto de línea */

}


.symptoms-column ul li {
  list-style-type: none;
  font-size: 22px; /* Tamaño de fuente más grande */
}

.footer {
  background-color: #9DD9D2; /* Color de fondo gris oscuro */
  padding: 20px;
  text-align: center; /* Centrar el texto */
}

.footer p {
  margin-top: 12px; /* Espacio entre el section y los síntomas */
  color: black; /* Color del texto */
  font-size: 18px; /* Tamaño de fuente */
  text-decoration: underline; /* Subrayar el texto */
}

/* Media Queries para pantallas pequeñas */
@media (max-width: 768px) {
  .main-section {
    padding: 20px 10px; /* Ajustar el padding para pantallas más pequeñas */
  }

  .main-section h2 {
    font-size: 25px; /* Ajustar el tamaño de fuente */
  }

  .main-section h3 {
    font-size: 20px; /* Ajustar el tamaño de fuente */
  }

  .main-section p {
    font-size: 16px; /* Ajustar el tamaño de fuente */
  }

  .symptoms-container {
    padding: 20px 10px; /* Ajustar el padding para pantallas más pequeñas */
  }

  .symptoms-container h3 {
    font-size: 25px; /* Ajustar el tamaño de fuente */
  }

  .column-container {
    flex: 1 1 100%; /* Permitir que las columnas se ocupen todo el ancho en pantallas pequeñas */
    margin: 10px 0; /* Agregar un margen entre las columnas */
  }
}


</style>
