<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Activity, Ban, Bookmark, Footprints, Mail, RotateCcw, Settings, Trash2, UserMinus, UserPlus, Users, UserX,
} from 'lucide-vue-next'
import { api } from '@/api'
import { ponerTitulo } from '@/router'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { useConfirmar } from '@/stores/confirmar'
import { fecha, hace } from '@/utils'
import Avatar from '@/components/Avatar.vue'
import EstablecimientoCard from '@/components/EstablecimientoCard.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'
import ListaUsuarios from '@/components/ListaUsuarios.vue'

const props = defineProps({ username: { type: String, required: true } })

const auth = useAuth()
const avisos = useAvisos()
const confirmar = useConfirmar()
const route = useRoute()
const router = useRouter()

const perfil = ref(null)
const noExiste = ref(false)
const datos = reactive({}) // datos de cada pestaña, cargados la primera vez que se abre

const esMio = computed(() => auth.usuario?.username === props.username)
const PESTANAS = [
  { id: 'actividad', texto: 'Actividad', ruta: (u) => `/actividades?usuario=${u}&limite=30` },
  { id: 'favoritos', texto: 'Favoritos', ruta: (u) => `/usuarios/${u}/favoritos` },
  { id: 'visitados', texto: 'Visitados', ruta: (u) => `/usuarios/${u}/visitados` },
  { id: 'seguidores', texto: 'Seguidores', ruta: (u) => `/usuarios/${u}/seguidores` },
  { id: 'seguidos', texto: 'Siguiendo', ruta: (u) => `/usuarios/${u}/seguidos` },
]
const pestana = computed({
  get: () => (PESTANAS.some((p) => p.id === route.query.tab) ? route.query.tab : 'actividad'),
  set: (tab) => router.replace({ query: tab === 'actividad' ? {} : { tab } }),
})
const numeros = computed(() => ({
  visitados: perfil.value?.num_visitados,
  seguidores: perfil.value?.num_seguidores,
  seguidos: perfil.value?.num_seguidos,
}))

const TEXTOS = { publicacion: 'Ha añadido', comentario: 'Ha opinado sobre', visita: 'Ha visitado', like: 'Le gusta' }

async function cargarPestana(tab, forzar = false) {
  if (datos[tab] && !forzar) return
  const usuario = encodeURIComponent(props.username)
  datos[tab] = await api(PESTANAS.find((p) => p.id === tab).ruta(usuario)).catch(() => [])
}

async function cargar() {
  try {
    perfil.value = await api(`/usuarios/${props.username}`)
    ponerTitulo(perfil.value.username)
    cargarPestana(pestana.value)
  } catch (e) {
    if (e.status === 404) noExiste.value = true
    else avisos.error(e)
  }
}
watch(pestana, (tab) => cargarPestana(tab))

async function alternarSeguir() {
  if (!auth.autenticado) return router.push({ name: 'login', query: { volver: route.fullPath } })
  const siguiendo = perfil.value.siguiendo
  perfil.value.siguiendo = !siguiendo
  perfil.value.num_seguidores += siguiendo ? -1 : 1
  try {
    await api(`/usuarios/${props.username}/seguir`, { method: siguiendo ? 'DELETE' : 'PUT' })
    if (datos.seguidores) cargarPestana('seguidores', true)
  } catch (e) {
    perfil.value.siguiendo = siguiendo
    perfil.value.num_seguidores += siguiendo ? 1 : -1
    avisos.error(e)
  }
}

async function alternarSuspension() {
  const archivado = !perfil.value.archivado
  if (archivado) {
    const ok = await confirmar.pedir({
      titulo: `¿Suspender a ${props.username}?`,
      texto: 'No podrá iniciar sesión y su perfil dejará de verse hasta que restaures la cuenta.',
      accion: 'Suspender',
    })
    if (!ok) return
  }
  try {
    await api(`/usuarios/${props.username}/archivado`, { method: 'PATCH', body: { archivado } })
    perfil.value.archivado = archivado
    avisos.ok(archivado ? 'Cuenta suspendida' : 'Cuenta restaurada')
  } catch (e) {
    avisos.error(e)
  }
}

async function eliminarCuenta() {
  const ok = await confirmar.pedir({
    titulo: `¿Eliminar la cuenta de ${props.username}?`,
    texto: 'Se borrarán su perfil, sus opiniones y su actividad. No se puede deshacer.',
    accion: 'Eliminar cuenta',
  })
  if (!ok) return
  try {
    await api(`/usuarios/${props.username}`, { method: 'DELETE' })
    avisos.ok('Cuenta eliminada')
    router.push('/comunidad')
  } catch (e) {
    avisos.error(e)
  }
}

onMounted(cargar)
</script>

<template>
  <div v-if="noExiste" class="contenedor">
    <EstadoVacio :icono="UserX" titulo="Este perfil no existe" texto="Puede que la cuenta se haya eliminado o suspendido.">
      <RouterLink to="/comunidad" class="btn btn-primario">Ir a la comunidad</RouterLink>
    </EstadoVacio>
  </div>

  <div v-else-if="!perfil" class="contenedor">
    <div class="esqueleto" style="height: 220px"></div>
  </div>

  <div v-else>
    <section class="cabecera-perfil">
      <div class="interior">
        <Avatar :usuario="perfil" :tamano="112" />
        <div class="identidad">
          <h1>{{ perfil.nombre }} {{ perfil.apellidos }}</h1>
          <p class="suave">@{{ perfil.username }}, en la comunidad desde {{ fecha(perfil.creado_en) }}</p>
          <p v-if="perfil.rol === 'admin' || perfil.archivado" class="estados">
            <span v-if="perfil.rol === 'admin'" class="chip ok">Administración</span>
            <span v-if="perfil.archivado" class="chip peligro">Cuenta suspendida</span>
          </p>
          <div class="cifras">
            <button @click="pestana = 'actividad'"><strong>{{ perfil.num_opiniones }}</strong> {{ perfil.num_opiniones === 1 ? 'opinión' : 'opiniones' }}</button>
            <button @click="pestana = 'visitados'"><strong>{{ perfil.num_visitados }}</strong> {{ perfil.num_visitados === 1 ? 'visitado' : 'visitados' }}</button>
            <button @click="pestana = 'seguidores'"><strong>{{ perfil.num_seguidores }}</strong> {{ perfil.num_seguidores === 1 ? 'seguidor' : 'seguidores' }}</button>
            <button @click="pestana = 'seguidos'"><strong>{{ perfil.num_seguidos }}</strong> siguiendo</button>
          </div>
        </div>
        <div class="acciones">
          <RouterLink v-if="esMio" to="/ajustes" class="btn"><Settings :size="17" /> Editar perfil</RouterLink>
          <button v-else class="btn" :class="perfil.siguiendo ? '' : 'btn-primario'" @click="alternarSeguir">
            <component :is="perfil.siguiendo ? UserMinus : UserPlus" :size="17" />
            {{ perfil.siguiendo ? 'Siguiendo' : 'Seguir' }}
          </button>
        </div>
      </div>
    </section>

    <div class="contenedor cuerpo">
      <section v-if="auth.esAdmin && !esMio" class="moderacion">
        <div>
          <h2>Moderación</h2>
          <dl v-if="perfil.privado" class="privado pequeno">
            <div><dt>Email</dt><dd>{{ perfil.privado.email }}</dd></div>
            <div><dt>DNI</dt><dd>{{ perfil.privado.dni }}</dd></div>
            <div><dt>Teléfono</dt><dd>{{ perfil.privado.telefono }}</dd></div>
          </dl>
        </div>
        <div class="fila">
          <a v-if="perfil.privado" :href="`mailto:${perfil.privado.email}`" class="btn btn-pequeno"><Mail :size="16" /> Email</a>
          <button class="btn btn-pequeno" @click="alternarSuspension">
            <component :is="perfil.archivado ? RotateCcw : Ban" :size="16" />
            {{ perfil.archivado ? 'Restaurar' : 'Suspender' }}
          </button>
          <button class="btn btn-pequeno btn-peligro" @click="eliminarCuenta"><Trash2 :size="16" /> Eliminar</button>
        </div>
      </section>

      <div class="pestanas" role="tablist">
        <button
          v-for="p in PESTANAS"
          :key="p.id"
          role="tab"
          :aria-selected="pestana === p.id"
          @click="pestana = p.id"
        >
          {{ p.texto }}<span v-if="numeros[p.id] !== undefined" class="numero">{{ numeros[p.id] }}</span>
        </button>
      </div>

      <div v-if="!datos[pestana]" class="rejilla">
        <div v-for="n in 3" :key="n" class="esqueleto" style="height: 120px"></div>
      </div>

      <template v-else-if="pestana === 'actividad'">
        <ul v-if="datos.actividad.length" class="lista-limpia lista-filas actividad">
          <li v-for="a in datos.actividad" :key="a.id">
            <component :is="a.tipo === 'visita' ? Footprints : Activity" :size="18" class="icono" aria-hidden="true" />
            <span>
              {{ TEXTOS[a.tipo] }}
              <RouterLink :to="`/establecimientos/${a.establecimiento.id}`">{{ a.establecimiento.nombre }}</RouterLink>
              <span class="suave">en {{ a.establecimiento.localidad }}</span>
            </span>
            <span class="suave pequeno cuando">{{ hace(a.creado_en) }}</span>
          </li>
        </ul>
        <EstadoVacio v-else :icono="Activity" titulo="Sin actividad todavía"
          :texto="esMio ? 'Visita, opina o añade sitios y aparecerán aquí.' : 'Esta persona aún no ha participado.'" />
      </template>

      <template v-else-if="pestana === 'favoritos' || pestana === 'visitados'">
        <div v-if="datos[pestana].length" class="rejilla">
          <EstablecimientoCard v-for="e in datos[pestana]" :key="e.id" :establecimiento="e" />
        </div>
        <EstadoVacio
          v-else
          :icono="pestana === 'favoritos' ? Bookmark : Footprints"
          :titulo="pestana === 'favoritos' ? 'Sin favoritos' : 'Sin visitas'"
          :texto="esMio ? 'Cuando guardes un sitio o lo marques como visitado, aparecerá aquí.' : ''"
        >
          <RouterLink v-if="esMio" to="/establecimientos" class="btn btn-primario">Ver establecimientos</RouterLink>
        </EstadoVacio>
      </template>

      <template v-else>
        <ListaUsuarios v-if="datos[pestana].length" :usuarios="datos[pestana]" />
        <EstadoVacio v-else :icono="Users" :titulo="pestana === 'seguidores' ? 'Aún sin seguidores' : 'No sigue a nadie todavía'" />
      </template>
    </div>
  </div>
</template>

<style scoped>
.cabecera-perfil {
  background: var(--niebla);
}

.cabecera-perfil .interior {
  width: min(var(--ancho), 100% - 2 * var(--margen));
  margin-inline: auto;
  padding-block: 2.5rem;
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 1.5rem 2rem;
}

.identidad {
  flex: 1;
  min-width: 240px;
}

.identidad h1 {
  margin: 0;
}

.identidad p {
  margin: 0.25rem 0 0;
}

.estados {
  display: flex;
  gap: 0.4rem;
  margin-top: 0.6rem !important;
}

.cifras {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 1.5rem;
  margin-top: 1rem;
}

.cifras button {
  font: inherit;
  color: var(--texto-suave);
  border: 0;
  background: none;
  padding: 0;
  min-height: 44px;
  cursor: pointer;
}

.cifras button:hover {
  color: var(--carbon);
  text-decoration: underline;
}

.cifras strong {
  font-weight: 700;
  color: var(--carbon);
  margin-right: 0.15rem;
}

.cuerpo {
  padding-top: 0;
}

.moderacion {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-top: 1.5rem;
  padding: 1rem 1.25rem;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio);
}

.moderacion h2 {
  font-size: 1rem;
  margin: 0;
}

.privado {
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 1.5rem;
  margin: 0.35rem 0 0;
}

.privado dt {
  display: inline;
  color: var(--texto-suave);
  margin-right: 0.35rem;
}

.privado dd {
  display: inline;
  margin: 0;
}

.pestanas {
  margin-top: 1.5rem;
}

.actividad li {
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) auto;
  align-items: center;
  gap: 0.15rem 0.75rem;
}

.actividad li span .suave {
  margin-left: 0.3rem;
}

.icono {
  color: var(--turquesa-oscuro);
  flex-shrink: 0;
}

.cuando {
  margin-left: auto;
  white-space: nowrap;
}

@media (max-width: 600px) {
  .actividad li {
    grid-template-columns: 18px minmax(0, 1fr);
    align-items: start;
  }

  .actividad .icono {
    margin-top: 0.2rem;
  }

  .actividad .cuando {
    grid-column: 2;
    margin-left: 0;
  }

  .cabecera-perfil .interior {
    padding-block: 1.75rem;
  }

  .acciones {
    width: 100%;
  }

  .acciones > * {
    width: 100%;
  }
}
</style>
