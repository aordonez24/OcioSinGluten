<template>
  <div>
    <header>
      <div class="logo-container">
        <img src="../assets/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg" alt="Logo de la aplicación" class="logo">
        <h1 class="title">Ocio Sin Gluten</h1>
      </div>
      <nav>
        <ul class="additional-links">
          <li><a href="#">Enlace 5</a></li>
          <li><a href="#">Enlace 6</a></li>
        </ul>
        <ul>
          <li><router-link to="/usuario" @click="$router.push('/usuario')">Usuario</router-link></li>
          <li><router-link to="/registroUsuario" @click="$router.push('/registroUsuario')">Registro Usuario</router-link></li>
        </ul>
      </nav>
    </header>

    <section class="main-section">
      <h2>¿Qué es la celiaquía?</h2>
      <p>La enfermedad celíaca es una afección en la que el cuerpo no puede tolerar el gluten. El gluten es una proteína que se encuentra en el trigo, la cebada y el centeno. Cuando las personas con celiaquía consumen gluten, su sistema inmunológico ataca el revestimiento del intestino delgado, lo que puede causar una serie de síntomas y problemas de salud.</p>
    </section>
  </div>
</template>

<script>
export default {
  name: 'HelloWorld'
}
</script>

<style>
@import "../assets/css/inicio.css";
</style>
