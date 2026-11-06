import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@fontsource-variable/montserrat'
import './styles.css'

createApp(App).use(createPinia()).use(router).mount('#app')

// Uso sin conexión de la app instalada (solo en la versión compilada; en desarrollo estorbaría).
if (import.meta.env.PROD && 'serviceWorker' in navigator) {
  window.addEventListener('load', () => navigator.serviceWorker.register('/sw.js'))
}
