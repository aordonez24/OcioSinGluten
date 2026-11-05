<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowLeft, Bookmark, ChevronLeft, ChevronRight, Footprints, Heart, ImagePlus, MapPin,
  Navigation, Pencil, Phone, Share2, Store, Trash2, X,
} from 'lucide-vue-next'
import { api, conArchivo } from '@/api'
import { ponerTitulo } from '@/router'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'
import { useConfirmar } from '@/stores/confirmar'
import { hace } from '@/utils'
import { CATEGORIAS } from '@/catalogo'
import Distintivos from '@/components/Distintivos.vue'
import EstadoVacio from '@/components/EstadoVacio.vue'
import Estrellas from '@/components/Estrellas.vue'
import EstablecimientoForm from '@/components/EstablecimientoForm.vue'
import Opiniones from '@/components/Opiniones.vue'
import Portada from '@/components/Portada.vue'

const Mapa = defineAsyncComponent(() => import('@/components/Mapa.vue'))
const props = defineProps({ id: { type: Number, required: true } })

const auth = useAuth()
const avisos = useAvisos()
const confirmar = useConfirmar()
const router = useRouter()

const est = ref(null)
const noExiste = ref(false)
const editando = ref(false)
const errorEdicion = ref('')
const guardando = ref(false)
const subiendo = ref(false)
const ampliada = ref(null) // índice de la imagen abierta en grande

const base = `/establecimientos/${props.id}`
const direccion = computed(() => est.value && `${est.value.calle}, ${est.value.cod_postal} ${est.value.localidad}`)
const tieneCoords = computed(() => est.value?.latitud != null)
const urlComoLlegar = computed(() => {
  if (!est.value) return '#'
  const destino = tieneCoords.value
    ? `${est.value.latitud},${est.value.longitud}`
    : `${direccion.value}, ${est.value.provincia}, ${est.value.pais}`
  return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(destino)}`
})
const categoria = computed(() => CATEGORIAS[est.value?.categoria] ?? CATEGORIAS.otro)
const puedeBorrar = (autorId) => auth.esAdmin || auth.usuario?.id === autorId

async function cargar() {
  try {
    est.value = await api(base)
    ponerTitulo(est.value.nombre)
  } catch (e) {
    if (e.status === 404) noExiste.value = true
    else avisos.error(e)
  }
}

function exigirSesion(motivo) {
  if (auth.autenticado) return true
  avisos.error(`Inicia sesión para ${motivo}`)
  router.push({ name: 'login', query: { volver: router.currentRoute.value.fullPath } })
  return false
}

// Respuesta inmediata en pantalla; si el servidor falla, se deshace.
const MARCAS = {
  like: { campo: 'me_gusta', contador: 'num_likes', motivo: 'dar me gusta' },
  visita: { campo: 'visitado', contador: 'num_visitas', motivo: 'marcar tus visitas' },
  favorito: { campo: 'favorito', contador: null, motivo: 'guardar favoritos' },
}

async function alternar(marca) {
  const { campo, contador, motivo } = MARCAS[marca]
  if (!exigirSesion(motivo)) return
  const activo = est.value[campo]
  const aplicar = (valor) => {
    est.value[campo] = valor
    if (contador) est.value[contador] += valor ? 1 : -1
  }
  aplicar(!activo)
  try {
    await api(`${base}/${marca}`, { method: activo ? 'DELETE' : 'PUT' })
    if (marca === 'favorito') avisos.ok(activo ? 'Quitado de favoritos' : 'Guardado en favoritos')
    est.value.valoracion = (await api(base)).valoracion
  } catch (e) {
    aplicar(activo)
    avisos.error(e)
  }
}

async function compartir() {
  const datos = { title: est.value.nombre, text: `${est.value.nombre}, sin gluten en ${est.value.localidad}`, url: location.href }
  if (navigator.share) return navigator.share(datos).catch(() => {})
  await navigator.clipboard.writeText(location.href)
  avisos.ok('Enlace copiado al portapapeles')
}

async function subirImagen(evento) {
  const archivo = evento.target.files[0]
  evento.target.value = ''
  if (!archivo) return
  subiendo.value = true
  try {
    est.value.imagenes.push(await api(`${base}/imagenes`, { method: 'POST', form: conArchivo(archivo) }))
    avisos.ok('¡Gracias por compartir la foto!')
  } catch (e) {
    avisos.error(e)
  } finally {
    subiendo.value = false
  }
}

async function borrarImagen(imagen) {
  if (!(await confirmar.pedir({ titulo: '¿Borrar esta foto?', accion: 'Borrar foto' }))) return
  try {
    await api(`${base}/imagenes/${imagen.id}`, { method: 'DELETE' })
    est.value.imagenes = est.value.imagenes.filter((i) => i.id !== imagen.id)
    ampliada.value = null
  } catch (e) {
    avisos.error(e)
  }
}

// Tras opinar/editar/borrar una opinión se actualiza la valoración media.
async function refrescarValoracion() {
  const { valoracion, num_opiniones } = await api(base)
  Object.assign(est.value, { valoracion, num_opiniones })
}

async function guardarEdicion(datos) {
  guardando.value = true
  errorEdicion.value = ''
  try {
    est.value = await api(base, { method: 'PATCH', body: datos })
    editando.value = false
    avisos.ok('Cambios guardados')
  } catch (e) {
    errorEdicion.value = e.message
  } finally {
    guardando.value = false
  }
}

async function eliminar() {
  const ok = await confirmar.pedir({
    titulo: `¿Eliminar "${est.value.nombre}"?`,
    texto: 'Se borrarán también sus opiniones, fotos y actividad. No se puede deshacer.',
    accion: 'Eliminar establecimiento',
  })
  if (!ok) return
  try {
    await api(base, { method: 'DELETE' })
    avisos.ok('Establecimiento eliminado')
    router.push('/establecimientos')
  } catch (e) {
    avisos.error(e)
  }
}

function moverAmpliada(paso) {
  const total = est.value.imagenes.length
  ampliada.value = (ampliada.value + paso + total) % total
}

function teclado(e) {
  if (ampliada.value === null) return
  if (e.key === 'Escape') ampliada.value = null
  if (e.key === 'ArrowRight') moverAmpliada(1)
  if (e.key === 'ArrowLeft') moverAmpliada(-1)
}

onMounted(() => {
  cargar()
  window.addEventListener('keydown', teclado)
})
onBeforeUnmount(() => window.removeEventListener('keydown', teclado))
</script>

<template>
  <div v-if="noExiste" class="contenedor">
    <EstadoVacio :icono="Store" titulo="Este establecimiento no existe" texto="Puede que lo hayan eliminado.">
      <RouterLink to="/establecimientos" class="btn btn-primario">Explorar otros sitios</RouterLink>
    </EstadoVacio>
  </div>

  <div v-else-if="!est" class="contenedor">
    <div class="esqueleto" style="height: 380px"></div>
  </div>

  <div v-else class="contenedor">
    <RouterLink to="/establecimientos" class="volver"><ArrowLeft :size="16" /> Explorar</RouterLink>

    <div class="disposicion">
      <section class="tarjeta ficha">
        <Portada class="banner" :class="{ 'sin-foto': !est.portada_url }" :url="est.portada_url" :nombre="est.nombre" />

        <div class="contenido">
          <template v-if="!editando">
            <p class="categoria"><component :is="categoria.icono" :size="15" /> {{ categoria.texto }}</p>
            <div class="titulo">
              <h1>{{ est.nombre }}</h1>
              <div v-if="auth.esAdmin" class="fila">
                <button class="btn btn-icono" title="Editar" @click="editando = true"><Pencil :size="17" /></button>
                <button class="btn btn-icono btn-peligro" title="Eliminar" @click="eliminar"><Trash2 :size="17" /></button>
              </div>
            </div>
            <p class="resumen">
              <Estrellas :valor="est.valoracion" />
              <a href="#titulo-opiniones" class="suave">
                <strong v-if="est.valoracion">{{ est.valoracion.toFixed(1) }}</strong>
                {{ est.num_opiniones }} {{ est.num_opiniones === 1 ? 'opinión' : 'opiniones' }}
              </a>
              <span class="suave">· {{ est.num_visitas }} {{ est.num_visitas === 1 ? 'visita' : 'visitas' }}</span>
            </p>

            <div class="acciones">
              <button class="btn" :class="{ 'btn-activo': est.me_gusta }" :aria-pressed="est.me_gusta" @click="alternar('like')">
                <Heart :size="18" :fill="est.me_gusta ? 'currentColor' : 'none'" /> Me gusta · {{ est.num_likes }}
              </button>
              <button class="btn" :class="{ 'btn-activo': est.visitado }" :aria-pressed="est.visitado" @click="alternar('visita')">
                <Footprints :size="18" /> {{ est.visitado ? 'He estado' : '¿Has estado?' }}
              </button>
              <button class="btn" :class="{ 'btn-activo': est.favorito }" :aria-pressed="est.favorito" @click="alternar('favorito')">
                <Bookmark :size="18" :fill="est.favorito ? 'currentColor' : 'none'" /> {{ est.favorito ? 'Guardado' : 'Guardar' }}
              </button>
              <button class="btn btn-icono" title="Compartir" @click="compartir"><Share2 :size="18" /></button>
            </div>

            <Distintivos :establecimiento="est" detallado class="distintivos" />
            <p v-if="!est.todo_sin_gluten && !est.carta_sin_gluten && !est.cocina_separada && !est.acreditado" class="sin-datos suave pequeno">
              Aún no sabemos qué opciones sin gluten ofrece. Si has estado, cuéntalo en tu opinión.
            </p>

            <ul class="lista-limpia datos">
              <li><MapPin :size="18" /> {{ direccion }} · {{ est.provincia }}, {{ est.pais }}</li>
              <li><Phone :size="18" /> <a :href="`tel:${est.telefono}`">{{ est.telefono }}</a></li>
              <li v-if="est.creado_por" class="suave pequeno">
                <span>
                  Añadido por
                  <RouterLink :to="`/u/${est.creado_por.username}`">{{ est.creado_por.username }}</RouterLink>
                  {{ hace(est.creado_en) }}
                </span>
              </li>
            </ul>
          </template>

          <template v-else>
            <h2>Editar establecimiento</h2>
            <EstablecimientoForm
              :inicial="est"
              :enviando="guardando"
              :error="errorEdicion"
              cancelable
              @guardar="guardarEdicion"
              @cancelar="editando = false"
            />
          </template>
        </div>
      </section>

      <aside class="lateral">
        <Mapa
          v-if="tieneCoords"
          :puntos="[{ id: est.id, latitud: est.latitud, longitud: est.longitud, titulo: est.nombre }]"
          altura="260px"
        />
        <a :href="urlComoLlegar" target="_blank" rel="noopener" class="btn btn-oscuro btn-bloque">
          <Navigation :size="18" /> Cómo llegar
        </a>

        <section class="tarjeta">
          <div class="cabecera-pagina">
            <h2>Fotos <span class="suave pequeno">{{ est.imagenes.length }}</span></h2>
            <label v-if="auth.autenticado" class="btn btn-pequeno" :aria-disabled="subiendo">
              <ImagePlus :size="16" /> {{ subiendo ? 'Subiendo…' : 'Añadir' }}
              <input type="file" accept="image/*" class="solo-lectores" :disabled="subiendo" @change="subirImagen" />
            </label>
          </div>
          <div v-if="est.imagenes.length" class="galeria">
            <button v-for="(img, i) in est.imagenes" :key="img.id" class="miniatura" @click="ampliada = i">
              <img :src="img.url" :alt="`Foto ${i + 1} de ${est.nombre}`" loading="lazy" />
            </button>
          </div>
          <p v-else class="suave pequeno sin-fotos">
            Aún no hay fotos.
            <template v-if="auth.autenticado">¿Has estado? ¡Comparte la primera!</template>
          </p>
        </section>
      </aside>

      <Opiniones class="opiniones" :est-id="est.id" :valoracion="est.valoracion" @cambio="refrescarValoracion" />
    </div>

    <div v-if="ampliada !== null" class="visor" role="dialog" aria-modal="true" aria-label="Foto ampliada" @click.self="ampliada = null">
      <img :src="est.imagenes[ampliada].url" alt="" />
      <div class="visor-controles">
        <button v-if="est.imagenes.length > 1" class="btn btn-icono" aria-label="Anterior" @click="moverAmpliada(-1)"><ChevronLeft /></button>
        <span class="contador">{{ ampliada + 1 }} / {{ est.imagenes.length }}</span>
        <button v-if="est.imagenes.length > 1" class="btn btn-icono" aria-label="Siguiente" @click="moverAmpliada(1)"><ChevronRight /></button>
        <button v-if="puedeBorrar(est.imagenes[ampliada].autor_id)" class="btn btn-peligro" @click="borrarImagen(est.imagenes[ampliada])">
          <Trash2 :size="16" /> Borrar
        </button>
        <button class="btn btn-icono" aria-label="Cerrar" @click="ampliada = null"><X /></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.disposicion {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: minmax(0, 1fr) 340px;
  align-items: start;
}

.ficha {
  padding: 0;
  overflow: hidden;
}

.banner {
  aspect-ratio: 21 / 8;
}

/* Sin fotos: franja ilustrada baja en lugar de un gran bloque de color. */
.banner.sin-foto {
  aspect-ratio: auto;
  height: 120px;
}

.banner.sin-foto :deep(.inicial) {
  font-size: 3.5rem;
}

.contenido {
  padding: 1.5rem;
}

.categoria {
  margin: 0 0 0.25rem;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--coral-oscuro);
}

.resumen a {
  text-decoration: none;
}

.distintivos {
  margin-top: 1.5rem;
}

.sin-datos {
  margin: 1.25rem 0 0;
  padding: 0.6rem 0.85rem;
  border-radius: var(--radio-sm);
  background: var(--crema);
}

.titulo {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.titulo h1 {
  margin: 0;
}

.resumen {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin: 0.4rem 0 1.25rem;
}

.acciones {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.datos {
  display: grid;
  gap: 0.5rem;
  margin-top: 1.5rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--borde);
}

.datos li {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}

.lateral {
  display: grid;
  gap: 1rem;
}

.lateral .cabecera-pagina {
  margin-bottom: 0.85rem;
}

.galeria {
  display: grid;
  gap: 0.5rem;
  grid-template-columns: repeat(3, 1fr);
}

.miniatura {
  padding: 0;
  border: 0;
  cursor: zoom-in;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: var(--crema-2);
}

.miniatura img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.2s;
}

.miniatura:hover img {
  transform: scale(1.06);
}

.sin-fotos {
  margin: 0;
}

.opiniones {
  grid-column: 1;
}

.visor {
  position: fixed;
  inset: 0;
  background: rgb(20 12 6 / 90%);
  display: grid;
  place-items: center;
  align-content: center;
  gap: 1rem;
  z-index: 500;
  padding: 1rem;
}

.visor img {
  max-height: 78vh;
  max-width: 100%;
  border-radius: 8px;
}

.visor-controles {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.contador {
  color: var(--crema);
  font-weight: 600;
  min-width: 4em;
  text-align: center;
}

@media (max-width: 900px) {
  .disposicion {
    grid-template-columns: 1fr;
  }

  .banner:not(.sin-foto) {
    aspect-ratio: 16 / 8;
  }
}
</style>
