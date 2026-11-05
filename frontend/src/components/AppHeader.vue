<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Activity, ChevronDown, Inbox, LogOut, Plus, Settings, UserRound, Users } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import Avatar from '@/components/Avatar.vue'
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
  avisos.ok('Has cerrado sesión. ¡Hasta pronto!')
  router.push('/')
}
</script>

<template>
  <header class="cabecera">
    <div class="interior">
      <RouterLink to="/" class="marca">
        <img :src="logo" alt="" width="40" height="40" />
        <span>Ocio Sin Gluten</span>
      </RouterLink>

      <nav class="principal" aria-label="Principal">
        <RouterLink to="/establecimientos">Explorar</RouterLink>
        <RouterLink to="/actividad">Actividad</RouterLink>
        <template v-if="auth.autenticado">
          <RouterLink to="/comunidad">Comunidad</RouterLink>
          <RouterLink to="/chat">Chat</RouterLink>
        </template>
      </nav>

      <div class="derecha">
        <template v-if="auth.autenticado">
          <RouterLink to="/establecimientos/nuevo" class="btn btn-primario btn-pequeno añadir">
            <Plus :size="16" /> Añadir sitio
          </RouterLink>
          <div ref="menu" class="menu-usuario">
            <button
              class="disparador"
              :aria-expanded="menuAbierto"
              aria-haspopup="menu"
              @click="menuAbierto = !menuAbierto"
            >
              <Avatar :usuario="auth.usuario" :tamano="34" />
              <span class="nombre">{{ auth.usuario.username }}</span>
              <ChevronDown :size="16" class="flecha" />
            </button>
            <div v-if="menuAbierto" class="desplegable" role="menu">
              <div class="quien">
                <strong>{{ auth.usuario.nombre }} {{ auth.usuario.apellidos }}</strong>
                <span class="suave pequeno">{{ auth.usuario.email }}</span>
              </div>
              <RouterLink role="menuitem" :to="`/u/${auth.usuario.username}`"><UserRound :size="17" /> Mi perfil</RouterLink>
              <RouterLink role="menuitem" to="/ajustes"><Settings :size="17" /> Ajustes de la cuenta</RouterLink>
              <RouterLink role="menuitem" to="/comunidad" class="solo-movil"><Users :size="17" /> Comunidad</RouterLink>
              <RouterLink role="menuitem" to="/actividad" class="solo-movil"><Activity :size="17" /> Actividad</RouterLink>
              <RouterLink v-if="auth.esAdmin" role="menuitem" to="/consultas"><Inbox :size="17" /> Consultas recibidas</RouterLink>
              <button role="menuitem" @click="salir"><LogOut :size="17" /> Cerrar sesión</button>
            </div>
          </div>
        </template>
        <template v-else>
          <RouterLink to="/entrar" class="entrar">Entrar</RouterLink>
          <RouterLink to="/registro" class="btn btn-primario btn-pequeno">Crear cuenta</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.cabecera {
  background: var(--marron);
  color: var(--crema);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 0 rgb(255 255 255 / 6%);
}

.interior {
  width: min(1120px, 100% - 2rem);
  margin-inline: auto;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  height: var(--alto-cabecera);
}

.marca {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  color: inherit;
  text-decoration: none;
  font-weight: 800;
  font-size: 1.1rem;
  letter-spacing: -0.01em;
  white-space: nowrap;
}

.marca img {
  border-radius: 50%;
}

.principal {
  display: flex;
  gap: 0.15rem;
}

.principal a,
.entrar {
  color: var(--crema-2);
  text-decoration: none;
  padding: 0.45rem 0.8rem;
  border-radius: 999px;
  font-weight: 600;
  font-size: 0.95rem;
}

.principal a:hover,
.entrar:hover {
  color: #fff;
  background: rgb(255 255 255 / 8%);
}

.principal a.router-link-active {
  color: #fff;
  background: rgb(255 255 255 / 12%);
}

.derecha {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.menu-usuario {
  position: relative;
}

.disparador {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: 0;
  color: inherit;
  font: inherit;
  font-weight: 600;
  padding: 0.25rem 0.5rem 0.25rem 0.25rem;
  border-radius: 999px;
  cursor: pointer;
}

.disparador:hover {
  background: rgb(255 255 255 / 8%);
}

.flecha {
  opacity: 0.7;
}

.desplegable {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  min-width: 250px;
  background: var(--blanco);
  color: var(--texto);
  border: 1px solid var(--borde);
  border-radius: var(--radio);
  box-shadow: var(--sombra-alta);
  padding: 0.4rem;
  display: grid;
}

.quien {
  display: grid;
  padding: 0.6rem 0.75rem 0.7rem;
  border-bottom: 1px solid var(--borde);
  margin-bottom: 0.3rem;
}

.desplegable a,
.desplegable button {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  padding: 0.6rem 0.75rem;
  border-radius: var(--radio-sm);
  color: inherit;
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
  background: var(--crema-2);
}

.solo-movil {
  display: none !important;
}

@media (max-width: 760px) {
  .principal,
  .añadir,
  .entrar,
  .nombre,
  .flecha {
    display: none;
  }

  .solo-movil {
    display: flex !important;
  }
}
</style>
