<template>
  <cabecera-componente/>
  <div>
    <section class="main-section">
      <h2>¿Qué es la celiaquía?</h2>
      <p>La enfermedad celíaca es una afección en la que el cuerpo no puede tolerar el gluten. El gluten es una proteína que se encuentra en el trigo, la cebada y el centeno. Cuando las personas con celiaquía consumen gluten, su sistema inmunológico ataca el revestimiento del intestino delgado, lo que puede causar una serie de síntomas y problemas de salud.</p>
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
            </ul>
          </div>
        </div>
        <div class="column-container">
          <div class="right-column">
            <ul>
              <li>Erupciones cutáneas</li>
              <li>Dolor abdominal</li>
              <li>Anemia</li>
              <li>Dolor en las articulaciones</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div class="contactin">
      <div class="column">
        <h1>¿Tienes alguna pregunta sobre la celiaquía o los alimentos sin gluten?</h1>
        <p>¡Envíanos un mensaje y estaremos encantados de ayudarte!</p>
      </div>
      <div class="column">
        <form action="/submit-message" method="post">
          <label for="name">Nombre y apellidos:</label>
          <input type="text" id="name" name="name" required>
          <label for="email">Correo:</label>
          <input type="email" id="email" name="email" required>
          <label for="message">Escribe tu mensaje:</label>
          <textarea id="message" name="message" required></textarea>
          <button type="submit">Enviar</button>
        </form>
      </div>
      <div class="column">
        <p>¡También puedes seguirnos en nuestras redes sociales!</p>
        <div class="social-icons">
          <a href="#"><i class="fab fa-instagram"></i></a>
          <a href="#"><i class="fab fa-twitter"></i></a>
          <a href="#"><i class="fab fa-facebook"></i></a>
        </div>
      </div>
    </div>
  </div>
  <footer class="footer">
    <p>Ocio Sin Gluten - Todos los derechos reservados. Álvaro Ordóñez Romero</p>
  </footer>

</template>

<script>
import '@fortawesome/fontawesome-free/css/all.css';
import CabeceraComponente from "@/components/header.vue";

export default {
  name: 'Vista-Inicio',
  components: {
    CabeceraComponente
  }
}
</script>

<style>
@import "../assets/css/inicio.css";
</style>
