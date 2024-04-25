<template>
  <div>
    <header>
      <div class="logo-container">
        <img src="../assets/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg" alt="Logo de la aplicación" class="logo">
        <div class="title-container">
          <h1 class="title">Ocio Sin Gluten</h1>
        </div>
      </div>
      <nav>
        <ul class="main-links">
          <li><router-link :to="{ path: '/' }" class="inicio-link">Inicio</router-link></li>
          <p>
            Una innovadora solución para que la comunidad celíaca no se sienta discriminada a la hora de elegir un establecimiento en el que pasar su tiempo de ocio.
          </p>
        </ul>
      </nav>
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
  background-color: #7F7F7F;
  padding: 20px;
  box-shadow: 0px 2px 5px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center; /* Centra horizontalmente */
  text-align: center; /* Añade centrado horizontal a los elementos inline */
}


.logo-container {
  display: flex; /* Hacemos que el contenedor sea un flexbox */
  align-items: center; /* Centramos los elementos verticalmente */
}

.logo {
  width: 150px; /* Tamaño del logo */
  height: 150px;
  border-radius: 50%; /* Forma redonda */
  margin-right: 20px; /* Ajustamos el margen derecho */
}

.title-container {
  display: flex; /* Hacemos que el contenedor sea un flexbox */
  flex-direction: column; /* Apilamos los elementos verticalmente */
}

.title {
  font-size: 50px; /* Tamaño del título */
  margin-bottom: 5px; /* Margen inferior para separar el título del subtítulo */
}



nav {
  margin-left: 30px; /* Reducir el margen izquierdo */
}

nav ul.p {
  font-size: 70px; /* Tamaño del subtítulo */
  margin-top: 0; /* Eliminar cualquier margen superior predeterminado */
}

nav ul.main-links {
  list-style-type: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center; /* Alinea los elementos al inicio */
  margin-top: 10px; /* Ajusta el margen superior del contenedor de los botones */
  margin-left: 0;
}

nav ul.main-links li {
  margin-right: 10px; /* Ajusta el margen entre botones */
}

nav ul.main-links li:last-child {
  margin-right: 0; /* Quita el margen derecho del último botón */
}



/* Estilo para todos los enlaces */
nav ul li a {
  text-decoration: none;
  display: block;
  width: 100%;
  padding: 8px 12px;
  border-radius: 20px;
  color: black;
  background-color: transparent;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

nav ul li a:hover {
  background-color: #9DD9D2;
  border-color: transparent;
}

/* Estilo para el enlace de "Iniciar Sesión" */
nav ul li:nth-child(4) a {
  background-color: #9DD9D2; /* Color de fondo verde */
  color: white; /* Color de texto blanco */
  border-color: transparent; /* Sin borde */
}

/* Estilo para el enlace de "Registrar Usuario" */
nav ul li:nth-child(5) a {
  background-color: #7F7F7F; /* Color de fondo azul */
  color: white; /* Color de texto blanco */
  border-color: transparent; /* Sin borde */
}

nav ul li:nth-child(4) a:hover {
  background-color: #ffa500;
  border-color: transparent;
}

/* Estilo para el enlace de "Registrar Usuario" */
nav ul li:nth-child(5) a:hover {
  background-color: #ffa500;
  border-color: transparent;
}

</style>