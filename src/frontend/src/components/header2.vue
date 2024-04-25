<template>
  <div>
    <header>
      <div class="logo-container">
        <img src="../assets/images/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg" alt="Logo de la aplicación" class="logo">
        <div class="title-container">
          <h1 class="title">Ocio Sin Gluten</h1>
          <nav>
            <ul class="main-links">
              <li><router-link :to="{ path: '/' }" class="inicio-link">Inicio</router-link></li>
            </ul>
          </nav>
        </div>
      </div>
      <p>
        Una innovadora solución para que la comunidad celíaca no se sienta discriminada a la hora de elegir un establecimiento en el que pasar su tiempo de ocio.
      </p>
    </header>
  </div>
</template>

<script>
export default {
  name: 'cabecera-componente-2'
}

</script>

<style scoped>
header {
  background-color: #444444;
  padding: 20px;
  box-shadow: 0px 2px 5px rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column; /* Apilamos los elementos verticalmente */
  align-items: center; /* Centramos los elementos horizontalmente */
  text-align: center; /* Añade centrado horizontal a los elementos inline */
}

.logo-container {
  display: flex; /* Hacemos que el contenedor del logo sea un flexbox */
  align-items: center; /* Centramos los elementos verticalmente */
}

.logo {
  width: 150px; /* Tamaño del logo */
  height: 150px;
  border-radius: 50%; /* Forma redonda */
  margin-right: 20px; /* Ajustamos el margen derecho */
}

.title-container {
  display: flex; /* Hacemos que el contenedor del título sea un flexbox */
  flex-direction: column; /* Apilamos los elementos verticalmente */
  align-items: center; /* Centramos los elementos horizontalmente */
}

.title {
  color: #ffffff;
  font-size: 50px; /* Tamaño del título */
  margin-bottom: 15px; /* Margen inferior para separar el título del botón */
  white-space: nowrap; /* Evita que el texto se divida en varias líneas */
  margin-top: 15px;
}

p {
  color: #ffa500;
  font-size: 20px;
  line-height: 1.5; /* Ajustamos el espaciado entre líneas para que sea más amplio */
  margin-top: 10px; /* Aumentamos el margen superior */
  margin-bottom: 10px; /* Aumentamos el margen inferior */
}

nav {
  margin: 0; /* Quitamos el margen izquierdo */
}

nav ul.main-links {
  list-style-type: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center; /* Centramos los elementos horizontalmente */
  margin-top: 10px; /* Ajusta el margen superior del contenedor de los botones */
  margin-left: 0;
}

nav ul.main-links li {
  margin-right: 0; /* Quitamos el margen entre botones */
}

nav ul li a {
  background-color: #9DD9D2;
  text-decoration: none;
  display: block;
  width: 100%;
  padding: 8px 12px;
  border-radius: 20px;
  color: #000000;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

nav ul li a:hover {
  background-color: #ffa500;
  border-color: transparent;
}

</style>