<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { LocateFixed, Search } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { hace } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import ContactoForm from '@/components/ContactoForm.vue'
import DeslizarParaRecargar from '@/components/DeslizarParaRecargar.vue'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'

// El mapa de la portada solo existe en pantallas anchas: en móvil no se descarga Leaflet.
const Mapa = defineAsyncComponent(() => import('@/components/Mapa.vue'))
const consulta = window.matchMedia('(min-width: 900px)')
const ancho = ref(consulta.matches)
const alCambiar = (e) => (ancho.value = e.matches)
consulta.addEventListener('change', alCambiar)
onBeforeUnmount(() => consulta.removeEventListener('change', alCambiar))

const auth = useAuth()
const router = useRouter()
const busqueda = ref('')
const establecimientos = ref(null)
const estadisticas = ref(null)
const actividad = ref([])
const guardados = ref([])

const destacados = computed(() =>
  [...(establecimientos.value ?? [])]
    .sort((a, b) => (b.valoracion ?? -1) - (a.valoracion ?? -1) || b.num_opiniones - a.num_opiniones)
    .slice(0, 4),
)
const puntos = computed(() =>
  (establecimientos.value ?? []).map((e) => ({ ...e, titulo: e.nombre, subtitulo: `${e.localidad}, ${e.provincia}` })),
)

const TEXTOS = { publicacion: 'ha añadido', comentario: 'ha opinado sobre', visita: 'ha visitado', like: 'recomienda' }
const SINTOMAS = [
  'Diarrea crónica', 'Distensión abdominal', 'Pérdida de peso inexplicable', 'Fatiga',
  'Erupciones cutáneas', 'Dolor abdominal', 'Anemia', 'Dolor en las articulaciones',
]
const plural = (n, uno, varios) => `${n} ${n === 1 ? uno : varios}`

function buscar() {
  router.push({ path: '/establecimientos', query: busqueda.value.trim() ? { q: busqueda.value.trim() } : {} })
}

async function cargar() {
  const [e, s, a] = await Promise.allSettled([
    api('/establecimientos'),
    api('/estadisticas'),
    api('/actividades?limite=6'),
  ])
  establecimientos.value = e.value ?? []
  estadisticas.value = s.value ?? null
  actividad.value = a.value ?? []
}
onMounted(cargar)

// Con sesión, la portada empieza por los sitios que la persona ha guardado.
onMounted(async () => {
  if (!auth.autenticado) return
  guardados.value = await api(`/usuarios/${auth.usuario.username}/favoritos`).catch(() => [])
})
</script>

<template>
  <DeslizarParaRecargar :al-recargar="cargar" />
  <section class="hero">
    <div class="hero-interior">
      <div class="hero-texto">
        <h1>Disfrute de su tiempo de ocio sin gluten</h1>
        <p class="entradilla">
          Restaurantes, bares, cafeterías y heladerías con opciones sin gluten, recomendados por personas
          celíacas que ya han estado allí.
        </p>
        <form class="buscador-hero" role="search" @submit.prevent="buscar">
          <label class="campo">
            <span class="solo-lectores">Buscar establecimientos</span>
            <Search :size="20" aria-hidden="true" />
            <input v-model="busqueda" type="search" placeholder="Ciudad o nombre" />
          </label>
          <button class="btn buscar">Buscar</button>
        </form>
        <p class="enlaces-hero">
          <RouterLink :to="{ path: '/establecimientos', query: { orden: 'cerca' } }">
            <LocateFixed :size="17" aria-hidden="true" /> Ver los sitios más cercanos a mí
          </RouterLink>
        </p>
        <p v-if="estadisticas" class="cifras">
          Ya hay {{ plural(estadisticas.establecimientos, 'sitio sin gluten', 'sitios sin gluten') }}
          en {{ plural(estadisticas.provincias, 'provincia', 'provincias') }},
          con {{ plural(estadisticas.opiniones, 'opinión', 'opiniones') }} de la comunidad.
        </p>
      </div>
      <div v-if="ancho" class="hero-mapa">
        <Mapa
          v-if="establecimientos"
          :puntos="puntos"
          altura="100%"
          @seleccionar="(id) => router.push(`/establecimientos/${id}`)"
        />
      </div>
    </div>
  </section>

  <section v-if="!auth.autenticado" class="invitacion">
    <p>
      <RouterLink to="/entrar">Inicia sesión</RouterLink> o
      <RouterLink to="/registro">crea una cuenta nueva</RouterLink>
      para pertenecer a la comunidad de Ocio Sin Gluten: opinar, guardar tus sitios y hablar en el chat.
    </p>
  </section>

  <section v-if="guardados.length" class="banda guardados" aria-labelledby="titulo-guardados">
    <div class="interior">
      <div class="cabecera-pagina">
        <h2 id="titulo-guardados">Tus sitios guardados</h2>
        <RouterLink :to="`/u/${auth.usuario.username}?tab=favoritos`">Ver todos tus guardados</RouterLink>
      </div>
      <div class="rejilla">
        <EstablecimientoCard v-for="e in guardados.slice(0, 4)" :key="e.id" :establecimiento="e" />
      </div>
    </div>
  </section>

  <section class="banda" aria-labelledby="titulo-destacados">
    <div class="interior">
      <div class="cabecera-pagina">
        <h2 id="titulo-destacados">Los mejor valorados</h2>
        <RouterLink to="/establecimientos">Ver todos los establecimientos</RouterLink>
      </div>
      <div class="rejilla destacados">
        <template v-if="establecimientos === null">
          <div v-for="n in 4" :key="n" class="esqueleto" style="aspect-ratio: 4 / 4"></div>
        </template>
        <EstablecimientoCard v-for="e in destacados" v-else :key="e.id" :establecimiento="e" />
      </div>
    </div>
  </section>

  <section class="banda niebla">
    <div class="interior dos">
      <div class="celiaquia">
        <h2>¿Qué es la celiaquía?</h2>
        <p>
          Es una enfermedad en la que el cuerpo no tolera el gluten, una proteína del trigo, la cebada y el
          centeno. Al consumirlo, el sistema inmunitario daña el revestimiento del intestino delgado. Por eso
          importa tanto saber dónde se cocina sin riesgo de contaminación cruzada.
        </p>
        <h3>Síntomas más frecuentes</h3>
        <ul class="sintomas">
          <li v-for="s in SINTOMAS" :key="s">{{ s }}</li>
        </ul>
      </div>

      <div class="comunidad">
        <div class="cabecera-pagina">
          <h2>Ahora en la comunidad</h2>
          <RouterLink to="/actividad">Ver todas las actividades</RouterLink>
        </div>
        <ul class="lista-limpia actividad">
          <li v-for="a in actividad" :key="a.id">
            <Avatar :usuario="a.usuario" :tamano="40" />
            <p>
              <RouterLink :to="`/u/${a.usuario.username}`" class="quien">{{ a.usuario.username }}</RouterLink>
              {{ TEXTOS[a.tipo] }}
              <RouterLink :to="`/establecimientos/${a.establecimiento.id}`">{{ a.establecimiento.nombre }}</RouterLink>
              <span class="cuando">{{ hace(a.creado_en) }}</span>
            </p>
          </li>
          <li v-if="!actividad.length" class="suave">Todavía no hay actividad.</li>
        </ul>
      </div>
    </div>
  </section>

  <div class="banda">
    <div class="interior">
      <ContactoForm />
    </div>
  </div>
</template>

<style scoped>
/* Banda carbón: la sección principal del proyecto original. */
.hero {
  background: var(--carbon);
  color: #f4fffd;
}

.hero-interior {
  width: min(var(--ancho), 100% - 2 * var(--margen));
  margin-inline: auto;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr);
  gap: 3rem;
  align-items: stretch;
  padding-block: 4rem;
}

.hero-texto {
  align-self: center;
}

.hero h1 {
  color: #fff;
  font-size: clamp(2rem, 4.4vw, 3.25rem);
  line-height: 1.1;
  letter-spacing: -0.025em;
  margin-bottom: 1rem;
}

.entradilla {
  font-size: 1.125rem;
  line-height: 1.55;
  margin: 0 0 2rem;
  max-width: 36em;
  color: #e3ecea;
}

.buscador-hero {
  display: flex;
  gap: 0.5rem;
  max-width: 560px;
}

.campo {
  position: relative;
  flex: 1;
  display: block;
}

.campo svg {
  position: absolute;
  left: 0.95rem;
  top: 50%;
  translate: 0 -50%;
  color: var(--texto-suave);
  pointer-events: none;
}

.campo input {
  min-height: 52px;
  padding-left: 2.85rem;
  border: 0;
  font-size: 1.0625rem;
}

.campo input:focus {
  box-shadow: 0 0 0 3px var(--ambar);
}

/* El ámbar del original, reservado para la acción principal. */
.buscar {
  min-height: 52px;
  padding-inline: 1.5rem;
  background: var(--ambar);
  border-color: var(--ambar);
  color: var(--carbon);
  font-size: 1rem;
}

.buscar:hover {
  background: #ffd98f;
  border-color: #ffd98f;
}

.buscar:focus-visible {
  outline-color: #fff;
}

.enlaces-hero {
  margin: 1rem 0 0;
}

.enlaces-hero a {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 44px;
  color: var(--ambar);
  font-weight: 600;
}

.enlaces-hero a:hover {
  color: #fff;
}

.hero a:focus-visible {
  outline-color: var(--ambar);
}

.cifras {
  margin: 1.5rem 0 0;
  padding-top: 1.25rem;
  border-top: 1px solid rgb(255 255 255 / 14%);
  font-size: 0.9375rem;
  color: #c9d6d4;
  max-width: 36em;
}

.hero-mapa {
  min-height: 420px;
  border-radius: var(--radio);
  overflow: hidden;
  background: #444;
}

.guardados + .banda {
  padding-top: 0;
}

.invitacion {
  background: var(--turquesa-claro);
}

.invitacion p {
  width: min(var(--ancho), 100% - 2 * var(--margen));
  margin: 0 auto;
  padding-block: 1rem;
  font-weight: 500;
}

.invitacion a {
  color: var(--carbon);
  font-weight: 700;
}

.dos {
  display: grid;
  gap: 3rem 4rem;
  grid-template-columns: 1fr 1fr;
  align-items: start;
}

.celiaquia p {
  max-width: 38em;
}

.celiaquia h3 {
  margin-top: 1.75rem;
}

.sintomas {
  columns: 2;
  column-gap: 2rem;
  padding-left: 1.1rem;
  margin: 0;
}

.sintomas li {
  padding-block: 0.15rem;
  break-inside: avoid;
}

.sintomas li::marker {
  color: var(--turquesa-oscuro);
}

.actividad li {
  display: flex;
  align-items: flex-start;
  gap: 0.85rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--borde-fuerte);
}

.actividad li:last-child {
  border-bottom: 0;
}

.actividad p {
  margin: 0;
}

.quien {
  font-weight: 650;
  color: var(--carbon);
  text-decoration: none;
}

.cuando {
  display: block;
  font-size: 0.8125rem;
  color: var(--texto-suave);
}

@media (max-width: 900px) {
  .hero-interior {
    grid-template-columns: 1fr;
    padding-block: 2.5rem 3rem;
  }

  .dos {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 520px) {
  .buscar {
    padding-inline: 1rem;
  }

  .sintomas {
    columns: 1;
  }
}
</style>
