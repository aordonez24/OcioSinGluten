import { createRouter, createWebHistory } from 'vue-router';

import iniciaSesion from "../views/Login.vue";
import RegistroUsuario from "../views/registroUsuario.vue";
import Inicio from "../views/Inicio.vue";
import VistaDespuesLogin from "@/views/InicioSesionIniciada.vue";
import VistaPerfil from "../views/perfilUsuario.vue";
import SeguidoresYSeguidosUsuario from "@/views/seguidores-y-seguidos-Usuario.vue";
import perfilUsuario from "@/views/visitarPerfilDeUsuario.vue";
import comunidad from "@/views/Comunidad.vue";
import chat from "@/views/Chat-Vista.vue";
import establecimientos from "@/views/establecimientos.vue";
import establecimiento from "@/views/verEstablecimiento.vue";
import nuevoEstablecimiento from "@/views/NuevoEstablecimiento.vue";
import establecimientosSinSesion from "@/views/establecimientosSinSesion.vue";

const routes = [
    { path: '/', component: Inicio },
    { path: '/iniciaSesion', component: iniciaSesion },
    { path: '/registroUsuario', component: RegistroUsuario },
    { path: '/establecimientos', component: establecimientos },
    { path: '/establecimientosNoToken', component: establecimientosSinSesion },
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
        path: '/contactoIniciadoSesion',
        name: 'Contacto',
        component: VistaDespuesLogin, // Cambiado a VistaDespuesLogin, que contiene la sección de contacto
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
        path: '/iniciadoSesion',
        name: 'VistaDespuesLogin',
        component: VistaDespuesLogin,
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
