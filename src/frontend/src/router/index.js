import { createRouter, createWebHistory } from 'vue-router';

import iniciaSesion from "../views/Login.vue";
import RegistroUsuario from "../views/registroUsuario.vue";
import Inicio from "../views/Inicio.vue";

const routes = [
    { path: '/', component: Inicio },
    { path: '/iniciaSesion', component: iniciaSesion },
    { path: '/registroUsuario', component: RegistroUsuario },
];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
