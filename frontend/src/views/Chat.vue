<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { MessagesSquare, Send } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'
import { agruparPorDia } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'

const auth = useAuth()
const mensajes = ref([])
const texto = ref('')
const estado = ref('conectando') // conectando | conectado | desconectado
const lista = ref(null)
const ESTADOS = { conectando: 'Conectando…', conectado: 'En línea', desconectado: 'Reconectando…' }

let socket
let reintento
let cerrandoAdrede = false

const esMio = (m) => m.autor.username === auth.usuario?.username
// Agrupa por día y marca si un mensaje sigue a otro del mismo autor (para no repetir avatar).
const grupos = computed(() =>
  agruparPorDia(
    mensajes.value.map((m, i) => ({ ...m, seguido: mensajes.value[i - 1]?.autor.username === m.autor.username })),
  ),
)

async function bajar() {
  await nextTick()
  lista.value?.scrollTo({ top: lista.value.scrollHeight, behavior: 'smooth' })
}

function conectar() {
  const protocolo = location.protocol === 'https:' ? 'wss' : 'ws'
  socket = new WebSocket(`${protocolo}://${location.host}/api/chat?token=${encodeURIComponent(auth.token)}`)
  socket.onopen = () => (estado.value = 'conectado')
  socket.onmessage = ({ data }) => {
    const evento = JSON.parse(data)
    if (evento.tipo === 'historial') mensajes.value = evento.mensajes
    else mensajes.value.push(evento)
    bajar()
  }
  socket.onclose = ({ code }) => {
    estado.value = 'desconectado'
    if (code === 1008) return auth.cerrarSesion({ porElServidor: true }) // sesión caducada o cuenta suspendida
    if (!cerrandoAdrede) reintento = setTimeout(conectar, 3000) // reconexión automática
  }
}

function enviar() {
  if (!texto.value.trim() || estado.value !== 'conectado') return
  socket.send(JSON.stringify({ texto: texto.value }))
  texto.value = ''
}

const hora = (iso) => new Date(iso).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })

onMounted(conectar)
onBeforeUnmount(() => {
  cerrandoAdrede = true
  clearTimeout(reintento)
  socket?.close()
})
</script>

<template>
  <div class="contenedor medio">
    <div class="cabecera-pagina">
      <div>
        <h1>Chat de la comunidad</h1>
        <p>Pregunta, recomienda y conoce a otras personas celíacas.</p>
      </div>
      <span class="chip" :class="estado === 'conectado' ? 'ok' : 'peligro'">
        <span class="punto"></span> {{ ESTADOS[estado] }}
      </span>
    </div>

    <div class="chat">
      <div ref="lista" class="mensajes" aria-live="polite">
        <EstadoVacio v-if="!mensajes.length" :icono="MessagesSquare" titulo="Aún no hay mensajes" texto="Saluda a la comunidad o pregunta por un sitio." />
        <section v-for="g in grupos" :key="g.etiqueta">
          <p class="dia"><span>{{ g.etiqueta }}</span></p>
          <div
            v-for="m in g.elementos"
            :key="m.id"
            class="mensaje"
            :class="{ mio: esMio(m), seguido: m.seguido }"
          >
            <template v-if="!esMio(m)">
              <span v-if="m.seguido" class="avatar-hueco"></span>
              <RouterLink v-else :to="`/u/${m.autor.username}`" class="avatar-hueco">
                <Avatar :usuario="m.autor" :tamano="32" />
              </RouterLink>
            </template>
            <div class="burbuja">
              <RouterLink v-if="!esMio(m) && !m.seguido" :to="`/u/${m.autor.username}`" class="autor">
                {{ m.autor.username }}
              </RouterLink>
              <span class="texto">{{ m.texto }}</span>
              <time :datetime="m.creado_en">{{ hora(m.creado_en) }}</time>
            </div>
          </div>
        </section>
      </div>
      <form class="escribir" @submit.prevent="enviar">
        <input v-model="texto" maxlength="500" placeholder="Escribe un mensaje" aria-label="Mensaje" autocomplete="off" />
        <button class="btn btn-primario btn-icono" :disabled="estado !== 'conectado' || !texto.trim()" title="Enviar">
          <Send :size="18" /><span class="solo-lectores">Enviar</span>
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  height: min(calc(100dvh - 280px - var(--alto-barra-movil)), 660px);
  min-height: 380px;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio);
  overflow: hidden;
}

.punto {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.mensajes {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 1.25rem 1rem;
}

.dia {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 1.25rem 0 0.5rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--texto-suave);
}

.dia::before,
.dia::after {
  content: '';
  flex: 1;
  border-top: 1px solid var(--borde);
}

.mensaje {
  display: flex;
  align-items: flex-end;
  gap: 0.5rem;
  max-width: 80%;
  margin-top: 0.6rem;
}

.mensaje.seguido {
  margin-top: 0.2rem;
}

.mensaje.mio {
  margin-left: auto;
  justify-content: flex-end;
}

.avatar-hueco {
  width: 32px;
  flex-shrink: 0;
}

.burbuja {
  background: var(--niebla-2);
  padding: 0.45rem 0.85rem 0.35rem;
  border-radius: 14px 14px 14px 4px;
  overflow-wrap: anywhere;
  display: grid;
}

.mio .burbuja {
  background: var(--turquesa);
  color: var(--carbon);
  border-radius: 14px 14px 4px 14px;
}

.autor {
  font-size: 0.8125rem;
  font-weight: 700;
  color: var(--turquesa-oscuro);
  text-decoration: none;
}

time {
  font-size: 0.75rem;
  color: var(--texto-suave);
  justify-self: end;
}

.mio time {
  color: #2b4f4b;
}

.escribir {
  display: flex;
  gap: 0.5rem;
  padding: 0.85rem;
  border-top: 1px solid var(--borde);
  background: var(--niebla);
}
</style>
