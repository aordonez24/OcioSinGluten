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

<style scoped>
header {
  background-color: #444444;
  padding: 20px;
  box-shadow: 0px 2px 5px rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  margin-right: 20px;
}

.title-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.title {
  color: #ffffff;
  font-size: 50px;
  margin-bottom: 15px;
  white-space: nowrap;

  @media screen and (max-width: 768px) {
    font-size: 30px;
    margin-top: 5px;
  }
}

p {
  color: #ffa500;
  font-size: 20px;
  line-height: 1.5;
  margin-top: 10px;
  margin-bottom: 10px;
}

nav {
  margin: 0;
}

nav ul.main-links {
  list-style-type: none;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 10px;
  margin-left: 0;
}

nav ul.main-links li {
  margin-right: 0;
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
