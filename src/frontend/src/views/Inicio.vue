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
  font-family: 'Montserrat', sans-serif;
}

.main-section {
  background-color: #353535;
  padding: 80px;
  color: rgb(244, 255, 253);
  text-align: center;

}

.main-section h2 {
  color: rgb(244, 255, 253);
  text-align: center;
  margin-bottom: 20px;
  font-size: 45px;
  font-family: 'Montserrat', sans-serif;

}

.main-section h3 {
  color: #ffcc74;
  text-align: center;
  margin-top: 40px;
  font-size: 30px;
  font-family: 'Montserrat', sans-serif;
}

.main-section p {
  font-size: 26px;
  line-height: 1.4;
  max-width: 1000px;
  margin: 0 auto;
}

.symptoms-container {
  background-color: #ffffff; /* Color de fondo gris */
  margin-top: 0;
  padding: 40px;
}

.symptoms-container h3 {
  margin-top: -15px;
  text-align: center;
  margin-bottom: 30px;
  font-family: 'Montserrat', sans-serif;
  font-size: 45px;
}

.symptoms-columns {
  display: flex;
  justify-content: center;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}


.column-container {
  width: 20%;
  margin: 0 20px;
  text-align: center;
}

.left-column ul,
.right-column ul {
  padding-left: -10px;
  list-style-type: none;
  text-align: left;
}
.left-column ul li,
.right-column ul li {
  list-style-type: none;
  font-size: 22px;
  white-space: nowrap;

}


.symptoms-column ul li {
  list-style-type: none;
  font-size: 22px;
}

.footer {
  background-color: #9DD9D2;
  padding: 20px;
  text-align: center;
}

.footer p {
  margin-top: 12px;
  color: black;
  font-size: 18px;
  text-decoration: underline;
}

@media (max-width: 768px) {
  .main-section {
    padding: 20px 10px;
  }

  .main-section h2 {
    font-size: 25px;
  }

  .main-section h3 {
    font-size: 20px;
  }

  .main-section p {
    font-size: 16px;
  }

  .symptoms-container {
    padding: 20px 10px;
  }

  .symptoms-container h3 {
    font-size: 25px;
  }

  .column-container {
    flex: 1 1 100%;
    margin: 10px 0;
  }
}


</style>
