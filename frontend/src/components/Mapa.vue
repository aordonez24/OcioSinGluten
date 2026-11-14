<script setup>
// Leaflet + OpenStreetMap: sustituye a ArcGIS (esri-loader), que descargaba varios MB.
// Sirve tanto para un único local (ficha) como para todos los del listado.
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const props = defineProps({
  // [{ id, latitud, longitud, titulo, subtitulo }]
  puntos: { type: Array, required: true },
  altura: { type: String, default: '300px' },
})
const emit = defineEmits(['seleccionar'])

const contenedor = ref(null)
let mapa
let capa
let lienzo

function popup(punto) {
  const div = document.createElement('div')
  div.className = 'popup-local'
  const titulo = document.createElement('strong')
  titulo.textContent = punto.titulo
  const sub = document.createElement('span')
  sub.textContent = punto.subtitulo ?? ''
  const boton = document.createElement('button')
  boton.textContent = 'Ver ficha'
  boton.onclick = () => emit('seleccionar', punto.id)
  div.append(titulo, sub, boton)
  return div
}

function dibujar() {
  capa.clearLayers()
  const validos = props.puntos.filter((p) => p.latitud != null && p.longitud != null)
  const muchos = validos.length > 100 // con cientos de sitios, puntos más pequeños para que no se tapen
  for (const p of validos) {
    const marcador = L.circleMarker([p.latitud, p.longitud], {
      radius: muchos ? 6 : 10,
      color: '#fff',
      weight: muchos ? 2 : 3,
      // Con cientos de puntos, canvas: los dibuja sin crear un elemento SVG por cada uno.
      renderer: muchos ? (lienzo ??= L.canvas()) : undefined,
      fillColor: '#17675f',
      fillOpacity: 1,
    }).addTo(capa)
    if (props.puntos.length > 1) marcador.bindPopup(popup(p))
    else marcador.bindTooltip(p.titulo)
  }
  if (validos.length === 1) mapa.setView([validos[0].latitud, validos[0].longitud], 16)
  else if (validos.length > 1) mapa.fitBounds(validos.map((p) => [p.latitud, p.longitud]), { padding: [40, 40], maxZoom: 14 })
  else mapa.setView([40.2, -3.7], 5) // España
}

onMounted(() => {
  mapa = L.map(contenedor.value, { scrollWheelZoom: false })
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    maxZoom: 19,
  }).addTo(mapa)
  capa = L.layerGroup().addTo(mapa)
  dibujar()
})

watch(() => props.puntos, dibujar)
onBeforeUnmount(() => mapa?.remove())
</script>

<template>
  <div ref="contenedor" class="mapa" :style="{ height: altura }" role="region" aria-label="Mapa"></div>
</template>

<style scoped>
.mapa {
  border-radius: var(--radio);
  z-index: 0;
}

.mapa :deep(.popup-local) {
  display: grid;
  gap: 0.15rem;
  font-family: var(--fuente);
}

.mapa :deep(.popup-local span) {
  color: var(--texto-suave);
}

.mapa :deep(.popup-local button) {
  margin-top: 0.4rem;
  border: 0;
  background: var(--carbon);
  color: #fff;
  border-radius: var(--radio-sm);
  padding: 0.4rem 0.8rem;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}
</style>
