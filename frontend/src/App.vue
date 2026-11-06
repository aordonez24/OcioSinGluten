<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { WifiOff } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
import BarraMovil from '@/components/BarraMovil.vue'
import DialogoConfirmar from '@/components/DialogoConfirmar.vue'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { useNotificaciones } from '@/stores/notificaciones'

const auth = useAuth()
const avisos = useAvisos()
const notificaciones = useNotificaciones()
const route = useRoute()
const router = useRouter()

// Comprueba que la sesión guardada sigue siendo válida y refresca los datos (p. ej. el rol).
onMounted(() => {
  if (auth.autenticado) api('/auth/yo').then((u) => (auth.usuario = u)).catch(() => {})
})

// Sin conexión la app sigue funcionando con lo guardado (service worker); se avisa de ello.
const enLinea = ref(navigator.onLine)
const actualizarConexion = () => (enLinea.value = navigator.onLine)
window.addEventListener('online', actualizarConexion)
window.addEventListener('offline', actualizarConexion)
onBeforeUnmount(() => {
  window.removeEventListener('online', actualizarConexion)
  window.removeEventListener('offline', actualizarConexion)
})

// Las notificaciones se consultan solo mientras hay sesión.
watch(
  () => auth.autenticado,
  (autenticado) => (autenticado ? notificaciones.empezar() : notificaciones.parar()),
  { immediate: true },
)

// Si la sesión caduca estando en una página privada, se vuelve al login.
watch(
  () => auth.autenticado,
  (autenticado) => {
    if (!autenticado && (route.meta.sesion || route.meta.admin)) {
      avisos.error('Tu sesión ha caducado. Vuelve a iniciar sesión.')
      router.push({ name: 'login', query: { volver: route.fullPath } })
    }
  },
)
</script>

<template>
  <a href="#contenido" class="saltar">Saltar al contenido</a>
  <AppHeader />
  <p v-if="!enLinea" class="sin-conexion" role="status">
    <WifiOff :size="16" aria-hidden="true" />
    Sin conexión. Ves lo que se guardó la última vez que abriste cada página.
  </p>
  <main id="contenido">
    <RouterView :key="$route.path" />
  </main>
  <AppFooter />
  <BarraMovil />
  <DialogoConfirmar />

  <div class="avisos" aria-live="polite">
    <TransitionGroup name="aviso">
      <button v-for="a in avisos.lista" :key="a.id" class="aviso" :class="a.tipo" @click="avisos.cerrar(a.id)">
        {{ a.texto }}
      </button>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.saltar {
  position: absolute;
  left: 1rem;
  top: -100px;
  z-index: 200;
  background: var(--carbon);
  color: #fff;
  padding: 0.6rem 1rem;
  border-radius: var(--radio-sm);
}

.saltar:focus {
  top: 0.5rem;
}

.sin-conexion {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin: 0;
  padding: 0.55rem var(--margen);
  background: var(--carbon);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  text-align: center;
}

.avisos {
  position: fixed;
  bottom: calc(1rem + var(--alto-barra-movil));
  right: 1rem;
  display: grid;
  gap: 0.5rem;
  z-index: 1000;
}

.aviso {
  font: inherit;
  font-weight: 500;
  text-align: left;
  border: 0;
  border-left: 4px solid var(--turquesa);
  cursor: pointer;
  padding: 0.85rem 1.1rem;
  border-radius: var(--radio-sm);
  color: #fff;
  background: var(--carbon);
  box-shadow: 0 8px 24px rgb(0 0 0 / 18%);
  max-width: 380px;
}

.aviso.error {
  background: var(--rojo);
  border-left-color: #ffb4ab;
}

.aviso-enter-from,
.aviso-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.aviso-enter-active,
.aviso-leave-active {
  transition: opacity 0.2s, transform 0.2s;
}

@media (max-width: 760px) {
  .avisos {
    left: 1rem;
  }
}
</style>
