<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Search, MapPinned, MessageCircleHeart, Users, ArrowRight } from 'lucide-vue-next'
import { api } from '@/api'
import { useAuth } from '@/stores/auth'
import { hace } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import ContactoForm from '@/components/ContactoForm.vue'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'
import logo from '@/assets/logo.webp'

const auth = useAuth()
const router = useRouter()
const busqueda = ref('')
const establecimientos = ref(null)
const estadisticas = ref(null)
const actividad = ref([])

const destacados = computed(() =>
  [...(establecimientos.value ?? [])]
    .sort((a, b) => (b.valoracion ?? -1) - (a.valoracion ?? -1) || b.num_opiniones - a.num_opiniones)
    .slice(0, 4),
)

const PASOS = [
  { icono: Search, titulo: 'Descubre', texto: 'Busca restaurantes, bares y cafeterías con opciones sin gluten cerca de ti.' },
  { icono: MessageCircleHeart, titulo: 'Comparte', texto: 'Marca los sitios que visitas, deja tu opinión y sube fotos de los platos.' },
  { icono: Users, titulo: 'Conecta', texto: 'Sigue a otras personas celíacas y charla con la comunidad en el chat.' },
]
const TEXTOS = { publicacion: 'ha añadido', comentario: 'ha opinado sobre', visita: 'ha visitado', like: 'recomienda' }
const SINTOMAS = [
  'Diarrea crónica', 'Distensión abdominal', 'Pérdida de peso inexplicable', 'Fatiga',
  'Erupciones cutáneas', 'Dolor abdominal', 'Anemia', 'Dolor en las articulaciones',
]

function buscar() {
  router.push({ path: '/establecimientos', query: busqueda.value.trim() ? { q: busqueda.value.trim() } : {} })
}

onMounted(async () => {
  const [e, s, a] = await Promise.allSettled([
    api('/establecimientos'),
    api('/estadisticas'),
    api('/actividades?limite=5'),
  ])
  establecimientos.value = e.value ?? []
  estadisticas.value = s.value ?? null
  actividad.value = a.value ?? []
})
</script>

<template>
  <section class="hero">
    <div class="hero-interior">
      <div class="hero-texto">
        <span class="etiqueta">Comunidad celíaca</span>
        <h1>Sal a comer <span>sin miedo al gluten</span></h1>
        <p>
          Restaurantes, bares y sitios de ocio recomendados por personas celíacas que ya han estado allí.
        </p>
        <form class="buscador-hero" role="search" @submit.prevent="buscar">
          <Search :size="20" />
          <input v-model="busqueda" type="search" placeholder="Busca un sitio, una ciudad o una provincia…" aria-label="Buscar" />
          <button class="btn btn-primario">Buscar</button>
        </form>
        <p v-if="!auth.autenticado" class="unete">
          ¿Aún no formas parte? <RouterLink to="/registro">Crea tu cuenta gratis</RouterLink>
        </p>
      </div>
      <img :src="logo" alt="" width="220" height="220" class="hero-logo" />
    </div>
  </section>

  <section v-if="estadisticas" class="cifras" aria-label="La comunidad en cifras">
    <div><strong>{{ estadisticas.establecimientos }}</strong> sitios sin gluten</div>
    <div><strong>{{ estadisticas.provincias }}</strong> provincias</div>
    <div><strong>{{ estadisticas.opiniones }}</strong> opiniones</div>
    <div><strong>{{ estadisticas.miembros }}</strong> miembros</div>
  </section>

  <div class="contenedor secciones">
    <section>
      <div class="cabecera-pagina">
        <div>
          <h2>Los mejor valorados</h2>
          <p>Lo que más gusta a la comunidad ahora mismo.</p>
        </div>
        <RouterLink to="/establecimientos" class="btn btn-pequeno">Ver todos <ArrowRight :size="16" /></RouterLink>
      </div>
      <div class="rejilla">
        <template v-if="establecimientos === null">
          <div v-for="n in 4" :key="n" class="esqueleto" style="height: 250px"></div>
        </template>
        <EstablecimientoCard v-for="e in destacados" v-else :key="e.id" :establecimiento="e" />
      </div>
    </section>

    <section class="como">
      <h2>Cómo funciona</h2>
      <ol class="lista-limpia pasos">
        <li v-for="(p, i) in PASOS" :key="p.titulo" class="tarjeta">
          <span class="icono"><component :is="p.icono" :size="24" /></span>
          <h3><span class="suave">{{ i + 1 }}.</span> {{ p.titulo }}</h3>
          <p class="suave">{{ p.texto }}</p>
        </li>
      </ol>
    </section>

    <div class="dos">
      <section class="tarjeta">
        <div class="cabecera-pagina">
          <h2>Ahora en la comunidad</h2>
          <RouterLink to="/actividad" class="pequeno">Ver más</RouterLink>
        </div>
        <ul class="lista-limpia actividad">
          <li v-for="a in actividad" :key="a.id">
            <Avatar :usuario="a.usuario" :tamano="36" />
            <span>
              <RouterLink :to="`/u/${a.usuario.username}`" class="quien">{{ a.usuario.username }}</RouterLink>
              {{ TEXTOS[a.tipo] }}
              <RouterLink :to="`/establecimientos/${a.establecimiento.id}`">{{ a.establecimiento.nombre }}</RouterLink>
              <span class="suave pequeno"> · {{ hace(a.creado_en) }}</span>
            </span>
          </li>
          <li v-if="!actividad.length" class="suave">Todavía no hay actividad.</li>
        </ul>
      </section>

      <section class="tarjeta celiaquia">
        <h2>¿Qué es la celiaquía?</h2>
        <p>
          Es una enfermedad en la que el cuerpo no tolera el <strong>gluten</strong>, una proteína del trigo,
          la cebada y el centeno. Al consumirlo, el sistema inmunitario daña el intestino delgado. Por eso es
          tan importante saber dónde se cocina sin riesgo de contaminación cruzada.
        </p>
        <h3>Síntomas frecuentes</h3>
        <ul class="lista-limpia sintomas">
          <li v-for="s in SINTOMAS" :key="s" class="chip">{{ s }}</li>
        </ul>
      </section>
    </div>

    <ContactoForm />
  </div>
</template>

<style scoped>
.hero {
  background:
    radial-gradient(circle at 85% 40%, rgb(226 96 74 / 25%), transparent 45%),
    linear-gradient(135deg, var(--marron) 0%, var(--marron-2) 100%);
  color: var(--crema);
}

.hero-interior {
  width: min(1120px, 100% - 2rem);
  margin-inline: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
  padding-block: 4rem;
}

.hero-texto {
  max-width: 640px;
}

.etiqueta {
  display: inline-block;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: var(--naranja);
  margin-bottom: 0.75rem;
}

.hero h1 {
  font-size: clamp(2.1rem, 5.5vw, 3.4rem);
  line-height: 1.08;
}

.hero h1 span {
  color: var(--naranja);
}

.hero p {
  font-size: 1.15rem;
  color: var(--crema-2);
}

.buscador-hero {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #fff;
  border-radius: 999px;
  padding: 0.35rem 0.35rem 0.35rem 1.1rem;
  margin-top: 1.75rem;
  color: var(--texto-suave);
  box-shadow: var(--sombra-alta);
}

.buscador-hero input {
  border: 0;
  padding: 0.6rem 0.25rem;
  box-shadow: none;
}

.unete {
  font-size: 0.95rem !important;
}

.unete a {
  color: var(--naranja);
  font-weight: 600;
}

.hero-logo {
  border-radius: 50%;
  box-shadow: 0 0 0 10px rgb(255 255 255 / 6%), var(--sombra-alta);
}

.cifras {
  width: min(1120px, 100% - 2rem);
  margin: -1.75rem auto 0;
  position: relative;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  background: var(--blanco);
  border: 1px solid var(--borde);
  border-radius: var(--radio);
  box-shadow: var(--sombra);
}

.cifras div {
  padding: 1rem;
  text-align: center;
  color: var(--texto-suave);
  font-size: 0.9rem;
}

.cifras div + div {
  border-left: 1px solid var(--borde);
}

.cifras strong {
  display: block;
  font-size: 1.6rem;
  color: var(--coral-oscuro);
}

.secciones {
  display: grid;
  gap: 3rem;
}

.pasos {
  display: grid;
  gap: 1.25rem;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
}

.pasos h3 {
  margin: 0.75rem 0 0.25rem;
}

.pasos p {
  margin: 0;
}

.icono {
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: var(--coral-suave);
  color: var(--coral-oscuro);
}

.dos {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: 1fr 1fr;
  align-items: start;
}

.actividad li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.6rem 0;
}

.actividad li + li {
  border-top: 1px solid var(--borde);
}

.quien {
  font-weight: 700;
  color: inherit;
  text-decoration: none;
}

.sintomas {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
}

@media (max-width: 860px) {
  .hero-logo {
    display: none;
  }

  .hero-interior {
    padding-block: 2.5rem 3.5rem;
  }

  .dos {
    grid-template-columns: 1fr;
  }

  .cifras {
    grid-template-columns: repeat(2, 1fr);
  }

  .cifras div:nth-child(3) {
    border-left: 0;
  }

  .cifras div:nth-child(n + 3) {
    border-top: 1px solid var(--borde);
  }
}

@media (max-width: 520px) {
  .buscador-hero .btn {
    padding-inline: 0.85rem;
  }
}
</style>
