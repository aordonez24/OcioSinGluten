<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { Activity, Footprints, Heart, MessageCircle, Search, Store, Users } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { agruparPorDia, coincide } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import DeslizarParaRecargar from '@/components/DeslizarParaRecargar.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'

const POR_PAGINA = 40
const TIPOS = {
  publicacion: { texto: 'ha añadido', filtro: 'Nuevos sitios', icono: Store },
  comentario: { texto: 'ha opinado sobre', filtro: 'Opiniones', icono: MessageCircle },
  visita: { texto: 'ha visitado', filtro: 'Visitas', icono: Footprints },
  like: { texto: 'recomienda', filtro: 'Me gusta', icono: Heart },
}

const auth = useAuth()
const actividades = ref(null)
const soloSiguiendo = ref(false) // "De quienes sigo"
const hayMas = ref(false)
const cargando = ref(false)
const busqueda = ref('')
const tipo = ref('')

const grupos = computed(() =>
  agruparPorDia(
    (actividades.value ?? [])
      .filter((a) => !tipo.value || a.tipo === tipo.value)
      .filter((a) => coincide(busqueda.value, a.usuario.username, a.establecimiento.nombre, a.establecimiento.localidad)),
  ),
)
const hora = (iso) => new Date(iso).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })

async function cargarMas() {
  cargando.value = true
  const ultimo = actividades.value?.at(-1)
  const parametros = new URLSearchParams({ limite: POR_PAGINA })
  if (ultimo) parametros.set('antes_de', ultimo.id)
  if (soloSiguiendo.value) parametros.set('siguiendo', 'true')
  const nuevas = await api(`/actividades?${parametros}`).catch(() => [])
  actividades.value = [...(actividades.value ?? []), ...nuevas]
  hayMas.value = nuevas.length === POR_PAGINA
  cargando.value = false
}

watch(soloSiguiendo, () => {
  actividades.value = null
  cargarMas()
})

onMounted(cargarMas)

async function recargar() {
  actividades.value = null
  await cargarMas()
}
</script>

<template>
  <DeslizarParaRecargar :al-recargar="recargar" />
  <div class="contenedor medio">
    <div class="cabecera-pagina">
      <div>
        <h1>Actividades</h1>
        <p>Lo último que ha hecho la comunidad: sitios nuevos, opiniones, visitas y recomendaciones.</p>
      </div>
    </div>

    <div v-if="auth.autenticado" class="interruptor" role="group" aria-label="De quién">
      <button :aria-pressed="!soloSiguiendo" @click="soloSiguiendo = false">Toda la comunidad</button>
      <button :aria-pressed="soloSiguiendo" @click="soloSiguiendo = true">De quienes sigo</button>
    </div>

    <div class="filtros">
      <label class="buscador">
        <span class="solo-lectores">Filtrar</span>
        <Search :size="18" aria-hidden="true" />
        <input v-model="busqueda" type="search" placeholder="Filtrar por persona o sitio" />
      </label>
      <div class="chips" role="group" aria-label="Tipo de actividad">
        <button :aria-pressed="tipo === ''" @click="tipo = ''">Todo</button>
        <button v-for="(t, clave) in TIPOS" :key="clave" :aria-pressed="tipo === clave" @click="tipo = clave">
          {{ t.filtro }}
        </button>
      </div>
    </div>

    <div v-if="actividades === null">
      <div v-for="n in 5" :key="n" class="esqueleto" style="height: 48px; margin: 0.6rem 0"></div>
    </div>

    <EstadoVacio
      v-else-if="!grupos.length && soloSiguiendo && !busqueda && !tipo"
      :icono="Users"
      titulo="Aún no sigues a nadie con actividad"
      texto="Sigue a otras personas celíacas para ver aquí sus recomendaciones."
    >
      <RouterLink to="/comunidad" class="btn btn-primario">Ver la comunidad</RouterLink>
    </EstadoVacio>
    <EstadoVacio v-else-if="!grupos.length" :icono="Activity" titulo="Nada por aquí"
      texto="No hay actividad que coincida con los filtros." />

    <section v-for="g in grupos" :key="g.etiqueta" class="grupo">
      <h2>{{ g.etiqueta }}</h2>
      <ul class="lista-limpia lista-filas lista">
        <li v-for="a in g.elementos" :key="a.id">
          <RouterLink :to="`/u/${a.usuario.username}`" class="avatar-tipo">
            <Avatar :usuario="a.usuario" :tamano="42" />
            <span class="tipo" :class="a.tipo"><component :is="TIPOS[a.tipo].icono" :size="12" /></span>
          </RouterLink>
          <p>
            <RouterLink :to="`/u/${a.usuario.username}`" class="usuario">{{ a.usuario.username }}</RouterLink>
            {{ TIPOS[a.tipo].texto }}
            <RouterLink :to="`/establecimientos/${a.establecimiento.id}`">{{ a.establecimiento.nombre }}</RouterLink>
            <span class="suave">en {{ a.establecimiento.localidad }}</span>
          </p>
          <time class="suave pequeno" :datetime="a.creado_en">{{ hora(a.creado_en) }}</time>
        </li>
      </ul>
    </section>

    <div v-if="hayMas" class="mas">
      <button class="btn" :disabled="cargando" @click="cargarMas">{{ cargando ? 'Cargando…' : 'Ver actividades anteriores' }}</button>
    </div>
  </div>
</template>

<style scoped>
.interruptor {
  display: inline-flex;
  margin-bottom: 1rem;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio-sm);
  overflow: hidden;
}

.interruptor button {
  font: inherit;
  font-weight: 600;
  font-size: 0.9375rem;
  min-height: 42px;
  padding: 0.4rem 1rem;
  border: 0;
  background: var(--blanco);
  color: var(--carbon);
  cursor: pointer;
}

.interruptor button + button {
  border-left: 1px solid var(--borde-fuerte);
}

.interruptor button[aria-pressed='true'] {
  background: var(--carbon);
  color: #fff;
}

.filtros {
  display: grid;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.grupo h2 {
  font-size: 1rem;
  margin: 2rem 0 0;
  padding-bottom: 0.6rem;
}

.lista li {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 0.85rem;
  align-items: center;
}

.lista p {
  margin: 0;
}

.lista p .suave {
  margin-left: 0.3rem;
}

.avatar-tipo {
  position: relative;
}

.tipo {
  position: absolute;
  right: -4px;
  bottom: -4px;
  display: grid;
  place-items: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--blanco);
  color: #fff;
  background: var(--turquesa-oscuro);
}

.tipo.like {
  background: #b33a5a;
}

.tipo.comentario {
  background: var(--carbon);
}

.tipo.publicacion {
  background: var(--verde);
}

.usuario {
  font-weight: 650;
  color: var(--carbon);
  text-decoration: none;
}

.mas {
  text-align: center;
  margin-top: 2rem;
}
</style>
