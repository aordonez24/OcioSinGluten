import { createMemoryHistory, createRouter } from 'vue-router'

import Usuario from "../views/Usuario.vue";
import RegistroUsuario from "../views/registroUsuario.vue";

const routes = [
    { path: '/usuarios', component: Usuario },
    { path: '/registro', component: RegistroUsuario },
]

const router = createRouter({
    history: createMemoryHistory(),
    routes,
})

export default router