<script setup>
import { onMounted, ref } from 'vue'
import { Check, Flag, Inbox, Reply } from 'lucide-vue-next'
import { api } from '@/api'
import { useAvisos } from '@/stores/avisos'
import { hace } from '@/utils'
import { MOTIVOS_AVISO } from '@/catalogo'
import EstadoVacio from '@/components/EstadoVacio.vue'

const avisos = useAvisos()
const consultas = ref(null)

const mailto = (c) =>
  `mailto:${c.email}?subject=${encodeURIComponent('Respuesta de Ocio Sin Gluten')}` +
  `&body=${encodeURIComponent(`Hola ${c.nombre},\n\nEn respuesta a tu mensaje:\n> ${c.mensaje}\n\n`)}`

async function resolver(c) {
  try {
    await api(`/consultas/${c.id}`, { method: 'DELETE' })
    consultas.value = consultas.value.filter((x) => x.id !== c.id)
    avisos.ok('Consulta marcada como resuelta')
  } catch (e) {
    avisos.error(e)
  }
}

onMounted(async () => {
  consultas.value = await api('/consultas').catch(() => [])
})
</script>

<template>
  <div class="contenedor medio">
    <div class="cabecera-pagina">
      <div>
        <h1>Consultas recibidas</h1>
        <p>Avisos sobre establecimientos y mensajes del formulario de contacto.</p>
      </div>
      <span v-if="consultas?.length" class="chip">{{ consultas.length }} pendientes</span>
    </div>

    <div v-if="consultas === null" class="esqueleto" style="height: 140px"></div>
    <ul v-else-if="consultas.length" class="lista-limpia lista-filas lista">
      <li v-for="c in consultas" :key="c.id">
        <p class="meta">
          <strong>{{ c.nombre }}</strong>
          <a :href="`mailto:${c.email}`" class="pequeno">{{ c.email }}</a>
          <span class="suave pequeno">{{ hace(c.creado_en) }}</span>
        </p>
        <p v-if="c.establecimiento" class="aviso-sitio">
          <Flag :size="16" aria-hidden="true" />
          <span>
            <strong>{{ MOTIVOS_AVISO[c.motivo].resumen }}</strong> en
            <RouterLink :to="`/establecimientos/${c.establecimiento.id}`">{{ c.establecimiento.nombre }}</RouterLink>
            <span class="suave">({{ c.establecimiento.localidad }})</span>
          </span>
        </p>
        <blockquote v-if="c.mensaje">{{ c.mensaje }}</blockquote>
        <div class="fila">
          <a :href="mailto(c)" class="btn btn-pequeno btn-primario"><Reply :size="16" /> Responder por email</a>
          <button class="btn btn-pequeno" @click="resolver(c)"><Check :size="16" /> Marcar como resuelta</button>
        </div>
      </li>
    </ul>
    <EstadoVacio v-else :icono="Inbox" titulo="No hay consultas pendientes" texto="Las que lleguen desde el formulario de contacto aparecerán aquí." />
  </div>
</template>

<style scoped>
.lista > li {
  padding-block: 1.25rem;
}

.meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.25rem 0.85rem;
  align-items: baseline;
}

.aviso-sitio {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  margin: 0.6rem 0 0;
}

.aviso-sitio svg {
  margin-top: 0.25rem;
}

.aviso-sitio svg {
  color: var(--rojo);
  flex-shrink: 0;
}

.aviso-sitio a {
  font-weight: 600;
}

blockquote {
  margin: 0.75rem 0 1rem;
  padding: 0.75rem 1rem;
  background: var(--niebla);
  border-left: 3px solid var(--turquesa-oscuro);
  border-radius: 0 var(--radio-sm) var(--radio-sm) 0;
  white-space: pre-line;
}
</style>
