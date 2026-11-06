import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { esNativa, iniciarApp } from './plataforma'
import '@fontsource-variable/montserrat'
import './styles.css'

createApp(App).use(createPinia()).use(router).mount('#app')

iniciarApp(router) // botón atrás, barra de estado y pantalla de bienvenida (solo en la app nativa)

// Uso sin conexión de la app instalada desde la web (solo compilada; en desarrollo estorbaría).
// La app nativa ya lleva todos sus ficheros dentro y no lo necesita.
if (import.meta.env.PROD && !esNativa && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js'))
}
