import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import '@fontsource-variable/montserrat'
import './styles.css'

createApp(App).use(createPinia()).use(router).mount('#app')
