<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { MessageSquareText, Pencil, Star, ThumbsUp, Trash2 } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { useConfirmar } from '@/stores/confirmar'
import { hace, nota } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'
import Estrellas from '@/components/Estrellas.vue'
import SelectorEstrellas from '@/components/SelectorEstrellas.vue'

const props = defineProps({
  estId: { type: Number, required: true },
  valoracion: { type: Number, default: null },
})
const emit = defineEmits(['cambio'])

const auth = useAuth()
const avisos = useAvisos()
const confirmar = useConfirmar()
const route = useRoute()

const opiniones = ref(null)
const editando = ref(false)
const enviando = ref(false)
const borrador = reactive({ puntuacion: 0, mensaje: '' })

const ORDENES = {
  recientes: { texto: 'Más recientes', fn: (a, b) => new Date(b.creado_en) - new Date(a.creado_en) },
  utiles: { texto: 'Más útiles', fn: (a, b) => b.num_utiles - a.num_utiles || new Date(b.creado_en) - new Date(a.creado_en) },
  mejores: { texto: 'Mejor valoradas', fn: (a, b) => b.puntuacion - a.puntuacion },
  peores: { texto: 'Peor valoradas', fn: (a, b) => a.puntuacion - b.puntuacion },
}
const orden = ref('recientes')

const mia = computed(() => opiniones.value?.find((o) => o.autor.username === auth.usuario?.username))
const resto = computed(() =>
  (opiniones.value?.filter((o) => o !== mia.value) ?? []).sort(ORDENES[orden.value].fn),
)
const mostrarFormulario = computed(() => auth.autenticado && (!mia.value || editando.value))
const distribucion = computed(() =>
  [5, 4, 3, 2, 1].map((n) => {
    const cantidad = opiniones.value?.filter((o) => o.puntuacion === n).length ?? 0
    return { n, cantidad, porcentaje: opiniones.value?.length ? (cantidad / opiniones.value.length) * 100 : 0 }
  }),
)

async function cargar() {
  opiniones.value = await api(`/establecimientos/${props.estId}/comentarios`).catch(() => [])
}

function empezarEdicion() {
  Object.assign(borrador, { puntuacion: mia.value.puntuacion, mensaje: mia.value.mensaje })
  editando.value = true
}

function cancelar() {
  editando.value = false
  Object.assign(borrador, { puntuacion: 0, mensaje: '' })
}

async function enviar() {
  if (!borrador.puntuacion) return avisos.error('Elige cuántas estrellas le das')
  enviando.value = true
  try {
    if (editando.value) {
      const editada = await api(`/comentarios/${mia.value.id}`, { method: 'PATCH', body: borrador })
      opiniones.value = opiniones.value.map((o) => (o.id === editada.id ? editada : o))
      avisos.ok('Opinión actualizada')
    } else {
      opiniones.value.unshift(await api(`/establecimientos/${props.estId}/comentarios`, { method: 'POST', body: borrador }))
      avisos.ok('¡Gracias por tu opinión!')
    }
    cancelar()
    emit('cambio')
  } catch (e) {
    avisos.error(e)
  } finally {
    enviando.value = false
  }
}

// "Me resultó útil": respuesta inmediata; si el servidor falla, se deshace.
async function alternarUtil(opinion) {
  if (!auth.autenticado) return avisos.error('Inicia sesión para valorar opiniones')
  const util = opinion.util
  opinion.util = !util
  opinion.num_utiles += util ? -1 : 1
  try {
    await api(`/comentarios/${opinion.id}/util`, { method: util ? 'DELETE' : 'PUT' })
  } catch (e) {
    opinion.util = util
    opinion.num_utiles += util ? 1 : -1
    avisos.error(e)
  }
}

async function borrar(opinion) {
  const ok = await confirmar.pedir({
    titulo: opinion === mia.value ? '¿Borrar tu opinión?' : `¿Borrar la opinión de ${opinion.autor.username}?`,
    texto: `"${opinion.mensaje}"`,
    accion: 'Borrar',
  })
  if (!ok) return
  try {
    await api(`/comentarios/${opinion.id}`, { method: 'DELETE' })
    opiniones.value = opiniones.value.filter((o) => o.id !== opinion.id)
    emit('cambio')
  } catch (e) {
    avisos.error(e)
  }
}

onMounted(cargar)
</script>

<template>
  <section class="opiniones" aria-labelledby="titulo-opiniones">
    <div class="cabecera">
      <h2 id="titulo-opiniones">Opiniones</h2>
      <label v-if="(opiniones?.length ?? 0) > 2" class="ordenar">
        <span class="solo-lectores">Ordenar opiniones</span>
        <select v-model="orden">
          <option v-for="(o, clave) in ORDENES" :key="clave" :value="clave">{{ o.texto }}</option>
        </select>
      </label>
    </div>

    <div v-if="opiniones?.length" class="resumen">
      <div class="media">
        <strong>{{ nota(valoracion) }}</strong>
        <Estrellas :valor="valoracion" :tamano="18" />
        <span class="suave pequeno">{{ opiniones.length }} {{ opiniones.length === 1 ? 'opinión' : 'opiniones' }}</span>
      </div>
      <ul class="lista-limpia barras" aria-label="Reparto de puntuaciones">
        <li v-for="d in distribucion" :key="d.n" :aria-label="`${d.n} estrellas: ${d.cantidad}`">
          <span class="estrellas-n" aria-hidden="true">{{ d.n }} <Star :size="12" fill="currentColor" /></span>
          <span class="barra" aria-hidden="true"><span :style="{ width: `${d.porcentaje}%` }"></span></span>
          <span class="suave pequeno" aria-hidden="true">{{ d.cantidad }}</span>
        </li>
      </ul>
    </div>

    <form v-if="mostrarFormulario" class="formulario nueva" @submit.prevent="enviar">
      <p class="cabecera-form">
        <strong>{{ editando ? 'Editar tu opinión' : '¿Has estado? Cuenta qué tal' }}</strong>
      </p>
      <SelectorEstrellas v-model="borrador.puntuacion" />
      <textarea
        v-model="borrador.mensaje"
        maxlength="500"
        rows="3"
        required
        aria-label="Tu opinión"
        placeholder="¿Tenían carta sin gluten? ¿Sabían lo que es la contaminación cruzada? ¿Qué pediste?"
      ></textarea>
      <div class="fila acciones-form">
        <span class="suave pequeno">{{ borrador.mensaje.length }} de 500 caracteres</span>
        <button v-if="editando" type="button" class="btn btn-pequeno" @click="cancelar">Cancelar</button>
        <button class="btn btn-primario btn-pequeno" :disabled="enviando || !borrador.mensaje.trim()">
          {{ editando ? 'Guardar' : 'Publicar opinión' }}
        </button>
      </div>
    </form>
    <p v-else-if="!auth.autenticado" class="aviso-sesion">
      <RouterLink :to="{ path: '/entrar', query: { volver: route.fullPath } }">Inicia sesión</RouterLink>
      para contar tu experiencia.
    </p>

    <div v-if="opiniones === null" class="esqueleto" style="height: 90px"></div>
    <ul v-else-if="opiniones.length" class="lista-limpia">
      <li v-for="o in mia && !editando ? [mia, ...resto] : resto" :key="o.id" class="opinion" :class="{ propia: o === mia }">
        <RouterLink :to="`/u/${o.autor.username}`" tabindex="-1" aria-hidden="true"><Avatar :usuario="o.autor" :tamano="40" /></RouterLink>
        <div>
          <p class="meta">
            <RouterLink :to="`/u/${o.autor.username}`" class="autor">{{ o === mia ? 'Tu opinión' : o.autor.username }}</RouterLink>
            <span class="suave">{{ hace(o.creado_en) }}</span>
            <span v-if="o.editado_en" class="suave">(editada)</span>
          </p>
          <Estrellas :valor="o.puntuacion" :tamano="14" />
          <p class="texto">{{ o.mensaje }}</p>
          <p class="valoracion-opinion">
            <button
              v-if="o !== mia"
              class="util"
              :class="{ activo: o.util }"
              :aria-pressed="o.util"
              @click="alternarUtil(o)"
            >
              <ThumbsUp :size="15" :fill="o.util ? 'currentColor' : 'none'" aria-hidden="true" />
              Útil<template v-if="o.num_utiles"> ({{ o.num_utiles }})</template>
            </button>
            <span v-else-if="o.num_utiles" class="suave pequeno">
              A {{ o.num_utiles }} {{ o.num_utiles === 1 ? 'persona le ha' : 'personas les ha' }} resultado útil
            </span>
          </p>
        </div>
        <div class="acciones">
          <button v-if="o === mia" class="btn btn-icono btn-fantasma" title="Editar opinión" @click="empezarEdicion">
            <Pencil :size="17" />
          </button>
          <button v-if="o === mia || auth.esAdmin" class="btn btn-icono btn-fantasma" title="Borrar opinión" @click="borrar(o)">
            <Trash2 :size="17" />
          </button>
        </div>
      </li>
    </ul>
    <EstadoVacio
      v-else
      :icono="MessageSquareText"
      titulo="Sin opiniones todavía"
      texto="Cuenta a la comunidad qué tal se come aquí sin gluten."
    />
  </section>
</template>

<style scoped>
.cabecera {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 0.75rem;
}

.cabecera h2 {
  margin: 0;
}

.ordenar select {
  width: auto;
  min-height: 40px;
  font-size: 0.9375rem;
}

.valoracion-opinion {
  margin-top: 0.4rem !important;
}

.util {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  min-height: 36px;
  padding: 0.25rem 0.7rem;
  margin-left: -0.7rem;
  border: 0;
  border-radius: var(--radio-sm);
  background: none;
  color: var(--texto-suave);
  font: inherit;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
}

.util:hover {
  background: var(--niebla);
  color: var(--carbon);
}

.util.activo {
  color: var(--turquesa-oscuro);
}

.resumen {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem 2.5rem;
  align-items: center;
  padding: 1.25rem 1.5rem;
  margin-bottom: 1.5rem;
  background: var(--niebla);
  border-radius: var(--radio);
}

.media {
  display: grid;
  justify-items: center;
  gap: 0.25rem;
}

.media strong {
  font-size: 2.5rem;
  line-height: 1;
  font-weight: 700;
  color: var(--carbon);
}

.barras {
  flex: 1;
  min-width: 200px;
  display: grid;
  gap: 0.25rem;
}

.barras li {
  display: grid;
  grid-template-columns: 2.5em 1fr 1.5em;
  align-items: center;
  gap: 0.5rem;
}

.estrellas-n {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.875rem;
  font-weight: 600;
}

.estrellas-n svg {
  color: var(--estrella);
}

.barra {
  height: 8px;
  border-radius: 4px;
  background: var(--niebla-2);
  overflow: hidden;
}

.barra span {
  display: block;
  height: 100%;
  background: var(--estrella);
}

.nueva {
  gap: 0.75rem;
  padding: 1.25rem;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio);
  margin-bottom: 1rem;
}

.cabecera-form {
  margin: 0;
}

.acciones-form {
  justify-content: flex-end;
}

.acciones-form > .suave {
  margin-right: auto;
}

.aviso-sesion {
  padding: 0.9rem 1.1rem;
  background: var(--niebla);
  border-radius: var(--radio-sm);
}

.opinion {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 0.85rem;
  padding: 1.1rem 0;
  border-top: 1px solid var(--borde);
}

.opinion.propia {
  background: var(--niebla);
  margin-inline: -1rem;
  padding-inline: 1rem;
  border-radius: var(--radio);
  border-top-color: transparent;
}

.opinion p {
  margin: 0;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  column-gap: 0.6rem;
  align-items: baseline;
}

.autor {
  font-weight: 650;
  color: var(--carbon);
  text-decoration: none;
}

.autor:hover {
  text-decoration: underline;
}

.texto {
  margin-top: 0.3rem !important;
}

.acciones {
  display: flex;
  align-items: flex-start;
}
</style>
