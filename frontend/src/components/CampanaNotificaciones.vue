<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Bell } from 'lucide-vue-next'
import { useNotificaciones } from '@/stores/notificaciones'
import { hace } from '@/utils'
import Avatar from '@/components/Avatar.vue'

const notificaciones = useNotificaciones()
const route = useRoute()
const abierto = ref(false)
const contenedor = ref(null)

const TEXTOS = {
  seguidor: 'ha empezado a seguirte',
  opinion: 'ha opinado sobre',
  util: 'ha marcado como útil tu opinión sobre',
}

function enlace(n) {
  if (n.tipo === 'seguidor' || !n.establecimiento) return `/u/${n.actor.username}`
  return { path: `/establecimientos/${n.establecimiento.id}`, hash: '#titulo-opiniones' }
}

async function alternar() {
  abierto.value = !abierto.value
  if (abierto.value) {
    await notificaciones.cargar()
    // Se muestran resaltadas las nuevas y, a la vez, se marcan como leídas en el servidor.
    notificaciones.marcarLeidas()
  }
}

function cerrarFuera(e) {
  if (abierto.value && !contenedor.value?.contains(e.target)) abierto.value = false
}
const escape = (e) => e.key === 'Escape' && (abierto.value = false)
watch(() => route.fullPath, () => (abierto.value = false))
onMounted(() => {
  document.addEventListener('click', cerrarFuera)
  document.addEventListener('keydown', escape)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', cerrarFuera)
  document.removeEventListener('keydown', escape)
})
</script>

<template>
  <div ref="contenedor" class="campana">
    <button
      class="boton"
      :aria-expanded="abierto"
      aria-haspopup="true"
      :aria-label="notificaciones.noLeidas ? `Notificaciones: ${notificaciones.noLeidas} sin leer` : 'Notificaciones'"
      @click="alternar"
    >
      <Bell :size="21" aria-hidden="true" />
      <span v-if="notificaciones.noLeidas" class="contador" aria-hidden="true">
        {{ notificaciones.noLeidas > 9 ? '9+' : notificaciones.noLeidas }}
      </span>
    </button>

    <div v-if="abierto" class="panel">
      <p class="titulo">Notificaciones</p>
      <ul v-if="notificaciones.lista.length" class="lista-limpia">
        <li v-for="n in notificaciones.lista" :key="n.id" :class="{ nueva: !n.leida }">
          <RouterLink :to="enlace(n)">
            <Avatar :usuario="n.actor" :tamano="36" />
            <span>
              <strong>{{ n.actor.username }}</strong> {{ TEXTOS[n.tipo] }}
              <strong v-if="n.tipo !== 'seguidor' && n.establecimiento">{{ n.establecimiento.nombre }}</strong>
              <span class="cuando">{{ hace(n.creado_en) }}</span>
            </span>
          </RouterLink>
        </li>
      </ul>
      <p v-else class="vacio">
        Aquí verás cuándo alguien te sigue, opina sobre un sitio que añadiste o valora tus opiniones.
      </p>
    </div>
  </div>
</template>

<style scoped>
.campana {
  position: relative;
}

.boton {
  position: relative;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border: 0;
  border-radius: 50%;
  background: none;
  color: var(--carbon);
  cursor: pointer;
}

.boton:hover,
.boton[aria-expanded='true'] {
  background: var(--niebla-2);
}

.contador {
  position: absolute;
  top: 4px;
  right: 2px;
  min-width: 19px;
  height: 19px;
  padding: 0 5px;
  border-radius: 999px;
  border: 2px solid var(--blanco);
  background: var(--rojo);
  color: #fff;
  font-size: 0.6875rem;
  font-weight: 700;
  line-height: 15px;
  text-align: center;
}

.panel {
  position: absolute;
  right: -3rem;
  top: calc(100% + 8px);
  width: min(380px, 100vw - 1.5rem);
  max-height: min(70vh, 520px);
  overflow-y: auto;
  background: var(--blanco);
  border: 1px solid var(--borde);
  border-radius: var(--radio);
  box-shadow: 0 12px 32px rgb(0 0 0 / 14%);
  padding: 0.4rem;
}

.titulo {
  margin: 0;
  padding: 0.6rem 0.75rem 0.5rem;
  font-weight: 700;
  color: var(--carbon);
}

li a {
  display: flex;
  align-items: flex-start;
  gap: 0.7rem;
  padding: 0.65rem 0.75rem;
  border-radius: var(--radio-sm);
  color: var(--texto);
  text-decoration: none;
  line-height: 1.4;
  font-size: 0.9375rem;
}

li a:hover {
  background: var(--niebla);
}

li.nueva a {
  background: var(--turquesa-claro);
}

li.nueva a:hover {
  background: #c6e8e4;
}

strong {
  font-weight: 650;
  color: var(--carbon);
}

.cuando {
  display: block;
  font-size: 0.8125rem;
  color: var(--texto-suave);
}

.vacio {
  margin: 0;
  padding: 0.5rem 0.75rem 0.85rem;
  color: var(--texto-suave);
  font-size: 0.9375rem;
}

@media (max-width: 760px) {
  .panel {
    position: fixed;
    left: 0.75rem;
    right: 0.75rem;
    top: calc(var(--alto-cabecera) + env(safe-area-inset-top) + 4px);
    width: auto;
  }
}
</style>
