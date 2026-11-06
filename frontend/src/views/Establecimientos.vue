<script setup>
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Clock, LayoutGrid, LocateFixed, Map, Search, SearchX, SlidersHorizontal, Store } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { coincide, distanciaKm } from '@/utils'
import { useUbicacion } from '@/composables'
import { useAvisos } from '@/stores/avisos'
import { CATEGORIAS, DISTINTIVOS, PRECIOS } from '@/catalogo'
import { estadoHorario, useAhora } from '@/horario'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'

// El mapa (Leaflet) solo se descarga si se elige la vista de mapa.
const Mapa = defineAsyncComponent(() => import('@/components/Mapa.vue'))

const auth = useAuth()
const avisos = useAvisos()
const { ubicacion, buscando, pedir: pedirUbicacion } = useUbicacion()
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
const precio = filtro('precio')
const abiertoTexto = filtro('abierto')
const abierto = computed({ get: () => abiertoTexto.value === '1', set: (v) => (abiertoTexto.value = v ? '1' : '') })
const ahora = useAhora()
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
  // Los sitios sin coordenadas van al final.
  cerca: (a, b) => (a.distancia ?? Infinity) - (b.distancia ?? Infinity),
}

// "Cerca de mí": al elegir ese orden se pide la ubicación; si se deniega, se vuelve al orden normal.
watch(
  orden,
  async (valor) => {
    if (valor !== 'cerca' || ubicacion.value) return
    try {
      await pedirUbicacion()
    } catch (e) {
      avisos.error(e)
      orden.value = 'valoracion'
    }
  },
  { immediate: true },
)

const provincias = computed(() =>
  [...new Set((establecimientos.value ?? []).map((e) => e.provincia))].sort((a, b) => a.localeCompare(b, 'es')),
)
const conDistancia = computed(() =>
  (establecimientos.value ?? []).map((e) => ({
    ...e,
    distancia: ubicacion.value && e.latitud != null ? distanciaKm(ubicacion.value, e) : null,
  })),
)
const filtrados = computed(() =>
  conDistancia.value
    .filter((e) => !provincia.value || e.provincia === provincia.value)
    .filter((e) => !categoria.value || e.categoria === categoria.value)
    .filter((e) => !precio.value || String(e.precio) === precio.value)
    .filter((e) => !abierto.value || estadoHorario(e, ahora.value)?.abierto)
    .filter((e) => distintivos.value.every((d) => e[d]))
    .filter((e) => coincide(busqueda.value, e.nombre, e.localidad, e.provincia, e.calle))
    .sort(ORDENES[orden.value] ?? ORDENES.valoracion),
)
const puntos = computed(() =>
  filtrados.value.map((e) => ({ ...e, titulo: e.nombre, subtitulo: `${e.localidad}, ${e.provincia}` })),
)
const filtrosAbiertos = ref(false)
const numFiltros = computed(
  () => [provincia.value, categoria.value, precio.value, abierto.value].filter(Boolean).length + distintivos.value.length,
)
const hayFiltros = computed(() =>
  Boolean(busqueda.value || provincia.value || categoria.value || precio.value || abierto.value || distintivos.value.length),
)

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
        <h1>Establecimientos sin gluten</h1>
        <p>
          <template v-if="establecimientos && filtrados.length === establecimientos.length">
            {{ establecimientos.length }} establecimientos recomendados por la comunidad
          </template>
          <template v-else-if="establecimientos">{{ filtrados.length }} de {{ establecimientos.length }} establecimientos</template>
          <template v-else>Cargando establecimientos…</template>
        </p>
      </div>
      <RouterLink v-if="auth.autenticado" to="/establecimientos/nuevo" class="btn btn-primario anadir">Añadir un sitio</RouterLink>
    </div>

    <div class="filtros">
      <div class="principal">
        <label class="buscador">
          <span class="solo-lectores">Buscar</span>
          <Search :size="18" aria-hidden="true" />
          <input v-model="textoBusqueda" type="search" placeholder="Nombre, localidad o calle" />
        </label>
        <button
          class="btn cerca"
          :class="{ 'btn-activo': orden === 'cerca' }"
          :aria-pressed="orden === 'cerca'"
          :disabled="buscando"
          @click="orden = orden === 'cerca' ? 'valoracion' : 'cerca'"
        >
          <LocateFixed :size="17" />
          <template v-if="buscando">Buscando…</template>
          <span v-else>Cerca<span class="de-mi"> de mí</span></span>
        </button>
        <div class="interruptor" role="group" aria-label="Vista">
          <button :aria-pressed="vista === 'lista'" title="Ver en lista" @click="vista = 'lista'">
            <LayoutGrid :size="18" /> <span class="etiqueta">Lista</span>
          </button>
          <button :aria-pressed="vista === 'mapa'" title="Ver en mapa" @click="vista = 'mapa'">
            <Map :size="18" /> <span class="etiqueta">Mapa</span>
          </button>
        </div>
        <button class="btn mostrar-filtros" :aria-expanded="filtrosAbiertos" @click="filtrosAbiertos = !filtrosAbiertos">
          <SlidersHorizontal :size="17" /> Filtros<span v-if="numFiltros" class="contador">{{ numFiltros }}</span>
        </button>
      </div>

      <div class="extra" :class="{ abierto: filtrosAbiertos }">
        <select v-model="provincia" aria-label="Provincia">
          <option value="">Todas las provincias</option>
          <option v-for="p in provincias" :key="p">{{ p }}</option>
        </select>
        <select v-model="categoria" aria-label="Tipo de sitio">
          <option value="">Todos los tipos</option>
          <option v-for="(c, clave) in CATEGORIAS" :key="clave" :value="clave">{{ c.texto }}</option>
        </select>
        <select v-model="precio" aria-label="Precio">
          <option value="">Cualquier precio</option>
          <option v-for="(p, n) in PRECIOS" :key="n" :value="String(n)">{{ p.simbolo }} {{ p.texto }}</option>
        </select>
        <select v-model="orden" aria-label="Ordenar por">
          <option value="valoracion">Mejor valorados</option>
          <option value="opiniones">Más opiniones</option>
          <option value="visitas">Más visitados</option>
          <option value="nombre">Nombre (A-Z)</option>
          <option value="cerca" hidden>Más cercanos</option>
        </select>
        <div class="chips distintivos" role="group" aria-label="Más filtros">
          <button :aria-pressed="abierto" title="Solo los sitios con horario que están abiertos en este momento" @click="abierto = !abierto">
            <Clock :size="15" /> Abierto ahora
          </button>
          <button
            v-for="d in DISTINTIVOS"
            :key="d.clave"
            :aria-pressed="distintivos.includes(d.clave)"
            :title="d.descripcion"
            @click="alternarDistintivo(d.clave)"
          >
            <component :is="d.icono" :size="15" /> {{ d.texto }}
          </button>
          <button v-if="hayFiltros" class="enlace quitar" @click="limpiarFiltros">Quitar filtros</button>
        </div>
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
      texto="Recomienda el primer sitio sin gluten."
    >
      <RouterLink v-if="auth.autenticado" to="/establecimientos/nuevo" class="btn btn-primario">Añadir el primero</RouterLink>
    </EstadoVacio>

    <EstadoVacio
      v-else-if="!filtrados.length"
      :icono="SearchX"
      titulo="Ningún establecimiento coincide"
      texto="Prueba con otra búsqueda o quita algún filtro. Si conoces un sitio que no está, puedes añadirlo."
    >
      <div class="fila">
        <button class="btn" @click="limpiarFiltros">Quitar filtros</button>
        <RouterLink v-if="auth.autenticado" to="/establecimientos/nuevo" class="btn btn-primario">
          Añadir un sitio nuevo
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
  display: grid;
  gap: 0.75rem;
  padding: 1rem;
  margin-bottom: 2rem;
  background: var(--niebla);
  border-radius: var(--radio);
}

.principal,
.extra {
  display: flex;
  flex-wrap: wrap;
  gap: 0.6rem;
  align-items: center;
}

.extra select {
  width: auto;
}

.interruptor {
  display: flex;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio-sm);
  overflow: hidden;
  background: #fff;
}

.interruptor button {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 42px;
  font: inherit;
  font-weight: 600;
  font-size: 0.9375rem;
  border: 0;
  background: none;
  padding: 0.4rem 0.9rem;
  cursor: pointer;
  color: var(--carbon);
}

.interruptor button + button {
  border-left: 1px solid var(--borde-fuerte);
}

.interruptor button[aria-pressed='true'] {
  background: var(--carbon);
  color: #fff;
}

.mostrar-filtros {
  display: none;
}

.contador {
  display: inline-grid;
  place-items: center;
  min-width: 20px;
  height: 20px;
  padding: 0 0.3rem;
  border-radius: 999px;
  background: var(--turquesa-oscuro);
  color: #fff;
  font-size: 0.75rem;
}

.distintivos .quitar {
  min-height: 40px;
  border: 0;
  background: none;
  padding-inline: 0.5rem;
}

.pie {
  text-align: center;
  margin-top: 2.5rem;
}

@media (max-width: 640px) {
  .anadir {
    display: none;
  }

  .principal .buscador {
    flex-basis: 100%;
  }

  .mostrar-filtros {
    display: inline-flex;
    margin-left: auto;
  }

  .principal {
    gap: 0.4rem;
  }

  .principal .btn {
    padding-inline: 0.75rem;
  }

  .interruptor button {
    padding-inline: 0.7rem;
  }

  .interruptor .etiqueta {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip: rect(0 0 0 0);
  }

  /* En móvil los filtros avanzados se pliegan para que los resultados se vean antes. */
  .extra {
    display: none;
  }

  .extra.abierto {
    display: flex;
  }

  .extra select {
    flex: 1 1 100%;
  }
}

@media (max-width: 380px) {
  .de-mi {
    display: none;
  }
}
</style>
