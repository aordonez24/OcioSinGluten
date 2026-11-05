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

  <div v-else class="contenedor">
    <section class="tarjeta cabecera-perfil">
      <Avatar :usuario="perfil" :tamano="104" />
      <div class="identidad">
        <h1>{{ perfil.nombre }} {{ perfil.apellidos }}</h1>
        <p class="suave">
          @{{ perfil.username }} · En la comunidad desde {{ fecha(perfil.creado_en) }}
        </p>
        <p class="chips">
          <span v-if="perfil.rol === 'admin'" class="chip ok">Administración</span>
          <span v-if="perfil.archivado" class="chip peligro">Cuenta suspendida</span>
        </p>
        <div class="cifras">
          <button @click="pestana = 'actividad'"><strong>{{ perfil.num_opiniones }}</strong> Opiniones</button>
          <button @click="pestana = 'visitados'"><strong>{{ perfil.num_visitados }}</strong> Visitados</button>
          <button @click="pestana = 'seguidores'"><strong>{{ perfil.num_seguidores }}</strong> Seguidores</button>
          <button @click="pestana = 'seguidos'"><strong>{{ perfil.num_seguidos }}</strong> Siguiendo</button>
        </div>
      </div>
      <div class="acciones">
        <RouterLink v-if="esMio" to="/ajustes" class="btn"><Settings :size="17" /> Editar perfil</RouterLink>
        <button v-else class="btn" :class="perfil.siguiendo ? '' : 'btn-primario'" @click="alternarSeguir">
          <component :is="perfil.siguiendo ? UserMinus : UserPlus" :size="17" />
          {{ perfil.siguiendo ? 'Siguiendo' : 'Seguir' }}
        </button>
      </div>
    </section>

    <section v-if="auth.esAdmin && !esMio" class="tarjeta moderacion">
      <div>
        <h2>Moderación</h2>
        <p v-if="perfil.privado" class="suave pequeno">
          {{ perfil.privado.email }} · DNI {{ perfil.privado.dni }} · Tel. {{ perfil.privado.telefono }}
        </p>
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
      <ul v-if="datos.actividad.length" class="lista-limpia tarjeta actividad">
        <li v-for="a in datos.actividad" :key="a.id">
          <component :is="a.tipo === 'visita' ? Footprints : Activity" :size="18" class="icono" />
          <span>
            {{ TEXTOS[a.tipo] }}
            <RouterLink :to="`/establecimientos/${a.establecimiento.id}`">{{ a.establecimiento.nombre }}</RouterLink>
            <span class="suave"> ({{ a.establecimiento.localidad }})</span>
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
        :texto="esMio ? 'Explora sitios y guárdalos o márcalos como visitados.' : ''"
      >
        <RouterLink v-if="esMio" to="/establecimientos" class="btn btn-primario">Explorar sitios</RouterLink>
      </EstadoVacio>
    </template>

    <template v-else>
      <ListaUsuarios v-if="datos[pestana].length" :usuarios="datos[pestana]" />
      <EstadoVacio v-else :icono="Users" :titulo="pestana === 'seguidores' ? 'Aún sin seguidores' : 'No sigue a nadie todavía'" />
    </template>
  </div>
</template>

<style scoped>
.cabecera-perfil {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.identidad {
  flex: 1;
  min-width: 240px;
}

.identidad h1 {
  margin: 0;
  font-size: 1.7rem;
}

.identidad p {
  margin: 0.2rem 0 0;
}

.cifras {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin: 1rem 0 0;
}

.cifras button {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  font: inherit;
  font-size: 0.78rem;
  color: var(--texto-suave);
  border: 1px solid var(--borde);
  background: var(--crema);
  border-radius: var(--radio-sm);
  padding: 0.45rem 0.9rem;
  cursor: pointer;
}

.cifras button:hover {
  border-color: var(--naranja);
}

.cifras strong {
  font-weight: 800;
  font-size: 1.15rem;
  color: var(--texto);
}

.moderacion {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.5rem;
  border-color: #f1c9a5;
  background: #fff8ef;
}

.moderacion h2 {
  font-size: 1.05rem;
  margin: 0;
}

.moderacion p {
  margin: 0.2rem 0 0;
}

.actividad {
  padding: 0.25rem 1.25rem;
}

.actividad li {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.8rem 0;
}

.actividad li + li {
  border-top: 1px solid var(--borde);
}

.icono {
  color: var(--naranja);
  flex-shrink: 0;
}

.cuando {
  margin-left: auto;
  white-space: nowrap;
}

@media (max-width: 600px) {
  .cabecera-perfil {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .chips {
    justify-content: center;
  }

  .cifras {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 0.35rem;
  }

  .cifras button {
    align-items: center;
    padding: 0.45rem 0.25rem;
  }

  .acciones {
    width: 100%;
  }

  .acciones > * {
    width: 100%;
  }
}
</style>
