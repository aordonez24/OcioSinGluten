<script setup>
// Cabecera blanca como la del proyecto original: logo, nombre y lema, y los enlaces en píldora
// turquesa. En móvil la navegación pasa a la barra inferior (BarraMovil).
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronDown, Inbox, LogOut, Plus, Settings, UserRound, Users, Activity } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import Avatar from '@/components/Avatar.vue'
import CampanaNotificaciones from '@/components/CampanaNotificaciones.vue'
import logo from '@/assets/logo.webp'

const auth = useAuth()
const avisos = useAvisos()
const route = useRoute()
const router = useRouter()
const menuAbierto = ref(false)
const menu = ref(null)

watch(() => route.fullPath, () => (menuAbierto.value = false))

function clicFuera(e) {
  if (menuAbierto.value && !menu.value?.contains(e.target)) menuAbierto.value = false
}
const escape = (e) => e.key === 'Escape' && (menuAbierto.value = false)
onMounted(() => {
  document.addEventListener('click', clicFuera)
  document.addEventListener('keydown', escape)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', clicFuera)
  document.removeEventListener('keydown', escape)
})

function salir() {
  auth.cerrarSesion()
  avisos.ok('Has cerrado sesión')
  router.push('/')
}
</script>

<template>
  <header class="cabecera">
    <div class="interior">
      <RouterLink to="/" class="marca">
        <img :src="logo" alt="" width="48" height="48" />
        <span class="nombre-marca">
          <strong>Ocio Sin Gluten</strong>
          <span class="lema">Disfrute de su tiempo de ocio sin gluten</span>
        </span>
      </RouterLink>

      <nav class="principal" aria-label="Principal">
        <RouterLink to="/" exact-active-class="activo">Inicio</RouterLink>
        <RouterLink to="/establecimientos" :class="{ activo: route.path.startsWith('/establecimientos') }">Establecimientos</RouterLink>
        <RouterLink to="/actividad" active-class="activo">Actividades</RouterLink>
        <template v-if="auth.autenticado">
          <RouterLink to="/comunidad" active-class="activo">Comunidad</RouterLink>
          <RouterLink to="/chat" active-class="activo">Chat</RouterLink>
        </template>
      </nav>

      <div class="derecha">
        <template v-if="auth.autenticado">
          <RouterLink to="/establecimientos/nuevo" class="btn btn-primario btn-pequeno anadir">
            <Plus :size="16" /> Añadir sitio
          </RouterLink>
          <CampanaNotificaciones />
          <div ref="menu" class="menu-usuario">
            <button
              class="disparador"
              :aria-expanded="menuAbierto"
              aria-haspopup="menu"
              @click="menuAbierto = !menuAbierto"
            >
              <Avatar :usuario="auth.usuario" :tamano="36" />
              <span class="nombre">{{ auth.usuario.username }}</span>
              <ChevronDown :size="16" class="flecha" />
            </button>
            <div v-if="menuAbierto" class="desplegable" role="menu">
              <div class="quien">
                <strong>{{ auth.usuario.nombre }} {{ auth.usuario.apellidos }}</strong>
                <span class="suave pequeno">{{ auth.usuario.email }}</span>
              </div>
              <RouterLink role="menuitem" :to="`/u/${auth.usuario.username}`"><UserRound :size="18" /> Mi perfil</RouterLink>
              <RouterLink role="menuitem" to="/ajustes"><Settings :size="18" /> Ajustes de la cuenta</RouterLink>
              <RouterLink role="menuitem" to="/comunidad" class="solo-movil"><Users :size="18" /> Comunidad</RouterLink>
              <RouterLink role="menuitem" to="/actividad" class="solo-movil"><Activity :size="18" /> Actividades</RouterLink>
              <RouterLink v-if="auth.esAdmin" role="menuitem" to="/consultas"><Inbox :size="18" /> Consultas recibidas</RouterLink>
              <button role="menuitem" @click="salir"><LogOut :size="18" /> Cerrar sesión</button>
            </div>
          </div>
        </template>
        <template v-else>
          <RouterLink to="/entrar" class="pildora entrar">Iniciar sesión</RouterLink>
          <RouterLink to="/registro" class="pildora crear">Crear cuenta</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.cabecera {
  background: var(--blanco);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 5px rgb(0 0 0 / 8%);
}

.interior {
  width: min(var(--ancho), 100% - 2 * var(--margen));
  margin-inline: auto;
  display: flex;
  align-items: center;
  gap: 2rem;
  height: var(--alto-cabecera);
}

.marca {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: var(--carbon);
  text-decoration: none;
  flex-shrink: 0;
}

.marca img {
  border-radius: 50%;
}

.nombre-marca {
  display: grid;
  line-height: 1.2;
}

.nombre-marca strong {
  font-size: 1.125rem;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.lema {
  font-size: 0.75rem;
  color: var(--texto-suave);
  font-weight: 500;
}

.principal {
  display: flex;
  gap: 0.25rem;
}

/* Píldoras de navegación: el turquesa del proyecto original marca la sección actual. */
.principal a,
.pildora {
  display: inline-flex;
  align-items: center;
  min-height: 40px;
  padding: 0.4rem 0.95rem;
  border-radius: 999px;
  color: var(--carbon);
  text-decoration: none;
  font-weight: 600;
  font-size: 0.9375rem;
  white-space: nowrap;
  transition: background-color 0.15s;
}

.principal a:hover {
  background: var(--niebla-2);
}

.principal a.activo {
  background: var(--turquesa);
}

.derecha {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.entrar {
  background: var(--turquesa);
}

.entrar:hover {
  background: #86cdc5;
  color: var(--carbon);
}

.crear {
  background: var(--carbon);
  color: #fff;
}

.crear:hover {
  background: var(--carbon-2);
  color: #fff;
}

.menu-usuario {
  position: relative;
}

.disparador {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-height: 44px;
  background: none;
  border: 0;
  color: var(--carbon);
  font: inherit;
  font-weight: 600;
  padding: 0.2rem 0.5rem 0.2rem 0.2rem;
  border-radius: 999px;
  cursor: pointer;
}

.disparador:hover,
.disparador[aria-expanded='true'] {
  background: var(--niebla-2);
}

.desplegable {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 260px;
  background: var(--blanco);
  border: 1px solid var(--borde);
  border-radius: var(--radio);
  box-shadow: 0 12px 32px rgb(0 0 0 / 14%);
  padding: 0.4rem;
  display: grid;
}

.quien {
  display: grid;
  padding: 0.6rem 0.75rem 0.75rem;
  border-bottom: 1px solid var(--borde);
  margin-bottom: 0.3rem;
  line-height: 1.35;
}

.desplegable a,
.desplegable button {
  display: flex;
  align-items: center;
  gap: 0.65rem;
  min-height: 44px;
  padding: 0.55rem 0.75rem;
  border-radius: var(--radio-sm);
  color: var(--carbon);
  text-decoration: none;
  font: inherit;
  font-weight: 500;
  background: none;
  border: 0;
  cursor: pointer;
  text-align: left;
}

.desplegable a:hover,
.desplegable button:hover {
  background: var(--niebla);
}

.solo-movil {
  display: none !important;
}

@media (max-width: 1180px) {
  .lema {
    display: none;
  }

  .interior {
    gap: 1.25rem;
  }
}

@media (max-width: 960px) {
  .nombre,
  .flecha {
    display: none;
  }
}

@media (max-width: 760px) {
  .principal,
  .anadir,
  .entrar {
    display: none;
  }

  .marca img {
    width: 40px;
    height: 40px;
  }

  .solo-movil {
    display: flex !important;
  }
}
</style>
