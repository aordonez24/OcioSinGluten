<script setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { LayoutGrid, Map, Search, SearchX, Store } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { coincide } from '@/utils'
import { CATEGORIAS, DISTINTIVOS } from '@/catalogo'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'

// El mapa (Leaflet) solo se descarga si se elige la vista de mapa.
const Mapa = defineAsyncComponent(() => import('@/components/Mapa.vue'))

const auth = useAuth()
const route = useRoute()
const router = useRouter()

const establecimientos = ref(null)
const error = ref('')

// Los filtros viven en la URL: se pueden compartir y se conservan al volver atrás.
const filtro = (clave, defecto = '') =>
  computed({
    get: () => route.query[clave] ?? defecto,
    set: (valor) =>
      router.replace({ query: { ...route.query, [clave]: valor && valor !== defecto ? valor : undefined } }),
  })
const busqueda = filtro('q')
const provincia = filtro('provincia')
const orden = filtro('orden', 'valoracion')
const vista = filtro('vista', 'lista')
const categoria = filtro('tipo')
const distintivosTexto = filtro('con')
const distintivos = computed(() => distintivosTexto.value.split(',').filter(Boolean))

function alternarDistintivo(clave) {
  const actuales = new Set(distintivos.value)
  actuales.has(clave) ? actuales.delete(clave) : actuales.add(clave)
  distintivosTexto.value = [...actuales].join(',')
}

// La búsqueda se escribe en un campo local y se pasa a la URL con un pequeño retardo.
const textoBusqueda = ref(busqueda.value)
let temporizador
watch(textoBusqueda, (v) => {
  clearTimeout(temporizador)
  temporizador = setTimeout(() => (busqueda.value = v.trim()), 250)
})

const ORDENES = {
  valoracion: (a, b) => (b.valoracion ?? -1) - (a.valoracion ?? -1) || b.num_opiniones - a.num_opiniones,
  opiniones: (a, b) => b.num_opiniones - a.num_opiniones,
  visitas: (a, b) => b.num_visitas - a.num_visitas,
  nombre: (a, b) => a.nombre.localeCompare(b.nombre, 'es'),
}

const provincias = computed(() =>
  [...new Set((establecimientos.value ?? []).map((e) => e.provincia))].sort((a, b) => a.localeCompare(b, 'es')),
)
const filtrados = computed(() =>
  (establecimientos.value ?? [])
    .filter((e) => !provincia.value || e.provincia === provincia.value)
    .filter((e) => !categoria.value || e.categoria === categoria.value)
    .filter((e) => distintivos.value.every((d) => e[d]))
    .filter((e) => coincide(busqueda.value, e.nombre, e.localidad, e.provincia, e.calle))
    .sort(ORDENES[orden.value] ?? ORDENES.valoracion),
)
const puntos = computed(() =>
  filtrados.value.map((e) => ({ ...e, titulo: e.nombre, subtitulo: `${e.localidad} · ${e.provincia}` })),
)
const hayFiltros = computed(() => Boolean(busqueda.value || provincia.value || categoria.value || distintivos.value.length))

function limpiarFiltros() {
  textoBusqueda.value = ''
  router.replace({ query: { orden: route.query.orden, vista: route.query.vista } })
}

onMounted(async () => {
  try {
    establecimientos.value = await api('/establecimientos')
  } catch (e) {
    error.value = e.message
    establecimientos.value = []
  }
})
</script>

<template>
  <div class="contenedor">
    <div class="cabecera-pagina">
      <div>
        <h1>Explorar sitios sin gluten</h1>
        <p>
          <template v-if="establecimientos">{{ filtrados.length }} de {{ establecimientos.length }} establecimientos</template>
          <template v-else>Cargando establecimientos…</template>
        </p>
      </div>
    </div>

    <div class="filtros tarjeta">
      <label class="buscador">
        <span class="solo-lectores">Buscar</span>
        <Search :size="18" />
        <input v-model="textoBusqueda" type="search" placeholder="Nombre, localidad o calle…" />
      </label>
      <select v-model="provincia" aria-label="Provincia">
        <option value="">Todas las provincias</option>
        <option v-for="p in provincias" :key="p">{{ p }}</option>
      </select>
      <select v-model="categoria" aria-label="Tipo de sitio">
        <option value="">Todos los tipos</option>
        <option v-for="(c, clave) in CATEGORIAS" :key="clave" :value="clave">{{ c.texto }}</option>
      </select>
      <select v-model="orden" aria-label="Ordenar por">
        <option value="valoracion">Mejor valorados</option>
        <option value="opiniones">Más opiniones</option>
        <option value="visitas">Más visitados</option>
        <option value="nombre">Nombre (A-Z)</option>
      </select>
      <div class="interruptor" role="group" aria-label="Vista">
        <button :aria-pressed="vista === 'lista'" title="Ver en lista" @click="vista = 'lista'">
          <LayoutGrid :size="18" /> Lista
        </button>
        <button :aria-pressed="vista === 'mapa'" title="Ver en mapa" @click="vista = 'mapa'">
          <Map :size="18" /> Mapa
        </button>
      </div>
      <div class="chips distintivos" role="group" aria-label="Filtrar por distintivos">
        <button
          v-for="d in DISTINTIVOS"
          :key="d.clave"
          :aria-pressed="distintivos.includes(d.clave)"
          :title="d.descripcion"
          @click="alternarDistintivo(d.clave)"
        >
          <component :is="d.icono" :size="15" /> {{ d.texto }}
        </button>
        <button v-if="hayFiltros" class="quitar" @click="limpiarFiltros">Quitar filtros</button>
      </div>
    </div>

    <p v-if="error" class="alerta">{{ error }}</p>

    <div v-if="establecimientos === null" class="rejilla">
      <div v-for="n in 8" :key="n" class="esqueleto" style="height: 260px"></div>
    </div>

    <EstadoVacio
      v-else-if="!establecimientos.length"
      :icono="Store"
      titulo="Todavía no hay establecimientos"
      texto="Sé la primera persona en recomendar un sitio sin gluten."
    >
      <RouterLink v-if="auth.autenticado" to="/establecimientos/nuevo" class="btn btn-primario">Añadir el primero</RouterLink>
    </EstadoVacio>

    <EstadoVacio
      v-else-if="!filtrados.length"
      :icono="SearchX"
      titulo="No hay resultados"
      texto="Prueba con otra búsqueda o quita los filtros."
    >
      <div class="fila">
        <button class="btn" @click="limpiarFiltros">Quitar filtros</button>
        <RouterLink v-if="auth.autenticado" to="/establecimientos/nuevo" class="btn btn-primario">
          ¿No está? Añádelo
        </RouterLink>
      </div>
    </EstadoVacio>

    <Mapa
      v-else-if="vista === 'mapa'"
      :puntos="puntos"
      altura="min(70vh, 640px)"
      @seleccionar="(id) => router.push(`/establecimientos/${id}`)"
    />

    <template v-else>
      <div class="rejilla">
        <EstablecimientoCard v-for="e in filtrados" :key="e.id" :establecimiento="e" />
      </div>
      <p v-if="hayFiltros" class="pie suave">
        ¿No encuentras lo que buscas? <button class="enlace" @click="limpiarFiltros">Quitar filtros</button>
      </p>
    </template>
  </div>
</template>

<style scoped>
.filtros {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding: 0.85rem;
  margin-bottom: 1.5rem;
}

.filtros select {
  width: auto;
}

.distintivos {
  flex-basis: 100%;
}

.distintivos button {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.distintivos .quitar {
  border-color: transparent;
  background: none;
  color: var(--coral-oscuro);
  text-decoration: underline;
}

.interruptor {
  display: flex;
  margin-left: auto;
  border: 1px solid var(--borde);
  border-radius: 999px;
  padding: 3px;
  background: #fff;
}

.interruptor button {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font: inherit;
  font-weight: 600;
  font-size: 0.9rem;
  border: 0;
  background: none;
  padding: 0.4rem 0.9rem;
  border-radius: 999px;
  cursor: pointer;
  color: var(--texto-suave);
}

.interruptor button[aria-pressed='true'] {
  background: var(--marron);
  color: var(--crema);
}

.pie {
  text-align: center;
  margin-top: 2rem;
}

.enlace {
  font: inherit;
  border: 0;
  background: none;
  color: var(--coral-oscuro);
  text-decoration: underline;
  cursor: pointer;
}

@media (max-width: 640px) {
  .filtros select {
    flex: 1;
  }

  .buscador {
    max-width: none;
    flex-basis: 100%;
  }

  .interruptor {
    margin-left: 0;
  }
}
</style>
