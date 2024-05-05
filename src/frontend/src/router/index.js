import { createRouter, createWebHistory } from 'vue-router';

import iniciaSesion from "../views/Login.vue";
import RegistroUsuario from "../views/Registro.vue";
import Inicio from "../views/Inicio.vue";
import VistaPerfil from "../views/PerfilDeUsuario.vue";
import SeguidoresYSeguidosUsuario from "@/views/SeguidoresSeguidos.vue";
import perfilUsuario from "@/views/VisitarPerfilDeUsuario.vue";
import comunidad from "@/views/Comunidad.vue";
import chat from "@/views/Chat-Vista.vue";
import establecimientos from "@/views/Establecimientos.vue";
import establecimiento from "@/views/PerfilEstablecimiento.vue";
import nuevoEstablecimiento from "@/views/NuevoEstablecimiento.vue";

const routes = [
    { path: '/', component: Inicio },
    { path: '/iniciaSesion', component: iniciaSesion },
    { path: '/registroUsuario', component: RegistroUsuario },
    { path: '/establecimientos', name:'establecimientos', component: establecimientos },
    {
        path: '/contacto',
        name: 'Contacto',
        component: Inicio, // Cambiado a Inicio, que contiene la sección de contacto
        beforeEnter(to, from, next) {
            // Desplazarse hasta la sección de contacto cuando se carga la página
            const elementoContacto = document.getElementById('contacto')
            if (elementoContacto) {
                elementoContacto.scrollIntoView({ behavior: 'smooth' })
            }
            next()
        }
    },
    {
        path: '/perfil/:username', // Agrega el parámetro dinámico ':username'
        name: 'Perfil',
        component: VistaPerfil,
    },
    {
        path: '/seguidosyseguidores/:username',
        name: 'SeguidosSeguidores',
        component: SeguidoresYSeguidosUsuario,
    },
    {
        path: '/perfilUsuario/:username',
        name: 'perfilOtroUsuario',
        component: perfilUsuario,
    },
    {
        path: '/comunidad',
        name: 'comunidad',
        component: comunidad,
    },
    {
        path: '/chatCon/:username',
        name: 'chatCon',
        component: chat,
    },
    {
        path: '/verEstabecimiento/:idEstablecimiento',
        name: 'verestablecimiento',
        component: establecimiento,
    },
    {
        path: '/nuevoEstablecimiento',
        name: 'nuevoEstablecimiento',
        component: nuevoEstablecimiento,
    },

];

const router = createRouter({
    history: createWebHistory(),
    routes,
});

export default router;
