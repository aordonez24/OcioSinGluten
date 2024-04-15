import { createRouter, createWebHistory } from 'vue-router';
import Usuario from "@/components/Usuario.vue";
import RegistroUsuario from "@/components/registroUsuario.vue";

const routes = [
    {
        path: "/usuario",
        name: "Usuario",
        component: Usuario
    },
    {
        path: "/registroUsuario",
        name: "RegistroUsuario",
        component: RegistroUsuario
    }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
});

export default router;
