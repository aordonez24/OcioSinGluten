import { createRouter, createWebHistory } from 'vue-router'
import { useAuth } from '@/stores/auth'

// Cada vista se carga bajo demanda: el navegador solo descarga el código de la página que abre.
// meta.titulo → título de la pestaña · meta.sesion → requiere sesión · meta.admin → solo admins
// meta.invitado → solo sin sesión (login/registro)
const routes = [
  { path: '/', component: () => import('@/views/Inicio.vue'), meta: { titulo: null } },
  { path: '/entrar', name: 'login', component: () => import('@/views/Login.vue'), meta: { titulo: 'Iniciar sesión', invitado: true } },
  { path: '/registro', component: () => import('@/views/Registro.vue'), meta: { titulo: 'Crear cuenta', invitado: true } },
  { path: '/establecimientos', component: () => import('@/views/Establecimientos.vue'), meta: { titulo: 'Establecimientos' } },
  {
    path: '/establecimientos/nuevo',
    component: () => import('@/views/NuevoEstablecimiento.vue'),
    meta: { titulo: 'Añadir establecimiento', sesion: true },
  },
  {
    path: '/establecimientos/:id(\\d+)',
    component: () => import('@/views/Establecimiento.vue'),
    props: (r) => ({ id: Number(r.params.id) }),
    meta: { titulo: 'Establecimiento' },
  },
  { path: '/actividad', component: () => import('@/views/Actividad.vue'), meta: { titulo: 'Actividades' } },
  { path: '/comunidad', component: () => import('@/views/Comunidad.vue'), meta: { titulo: 'Comunidad', sesion: true } },
  { path: '/chat', component: () => import('@/views/Chat.vue'), meta: { titulo: 'Chat', sesion: true } },
  { path: '/ajustes', component: () => import('@/views/Ajustes.vue'), meta: { titulo: 'Ajustes', sesion: true } },
  { path: '/consultas', component: () => import('@/views/Consultas.vue'), meta: { titulo: 'Consultas', admin: true } },
  { path: '/u/:username', component: () => import('@/views/Perfil.vue'), props: true, meta: { titulo: 'Perfil' } },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NoEncontrado.vue'), meta: { titulo: 'No encontrado' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, guardada) {
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    if (guardada) return guardada
    if (to.path === from.path) return false // solo cambian filtros/pestañas
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const auth = useAuth()
  if ((to.meta.sesion || to.meta.admin) && !auth.autenticado) {
    return { name: 'login', query: { volver: to.fullPath } }
  }
  if (to.meta.admin && !auth.esAdmin) return '/'
  if (to.meta.invitado && auth.autenticado) return '/'
})

/** Las vistas pueden afinar el título (p. ej. con el nombre del local). */
export function ponerTitulo(titulo) {
  document.title = titulo ? `${titulo} - Ocio Sin Gluten` : 'Ocio Sin Gluten - Disfrute de su tiempo de ocio sin gluten'
}

router.afterEach((to) => ponerTitulo(to.meta.titulo))

export default router
