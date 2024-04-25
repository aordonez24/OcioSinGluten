<template>
  <div>
    <header>
      <div class="logo-container">
        <img src="../assets/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg" alt="Logo de la aplicación" class="logo">
        <div class="title-container">
          <h1 class="title">Ocio Sin Gluten</h1>
          <li><router-link :to="{ path: '/' }">Inicio</router-link></li>
        </div>
      </div>
      <nav>
        <h2>
          <a>Una innovadora solución para que la comunidad celíaca no se sienta discriminada a la hora de elegir un establecimiento en el que pasar su tiempo de ocio.</a>
        </h2>
      </nav>
    </header>
  </div>
</template>

<script>
export default {
  name: 'cabecera-componente-2'
}

</script>

<style>

</style>