import Vue from 'vue'
import VueRouter from 'vue-router'
import AgregarUsuario from '../views/AgregarUsuario.vue'

Vue.use(VueRouter)

const routes = [
    {
        path: '/agregar-usuario',
        name: 'AgregarUsuario',
        component: AgregarUsuario
    }
]

const router = new VueRouter({
    mode: 'history',
    base: process.env.BASE_URL,
    routes
})

export default router
