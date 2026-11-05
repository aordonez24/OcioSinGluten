<script setup>
import { onMounted, ref } from 'vue'
import { Check, Inbox, Reply } from 'lucide-vue-next'
import { api } from '@/api'
import { useAvisos } from '@/stores/avisos'
import { hace } from '@/utils'
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
        <p>Mensajes enviados desde el formulario de contacto.</p>
      </div>
      <span v-if="consultas?.length" class="chip">{{ consultas.length }} pendientes</span>
    </div>

    <div v-if="consultas === null" class="esqueleto" style="height: 140px"></div>
    <ul v-else-if="consultas.length" class="lista-limpia lista">
      <li v-for="c in consultas" :key="c.id" class="tarjeta">
        <p class="meta">
          <strong>{{ c.nombre }}</strong>
          <span class="suave pequeno">{{ c.email }} · {{ hace(c.creado_en) }}</span>
        </p>
        <blockquote>{{ c.mensaje }}</blockquote>
        <div class="fila">
          <a :href="mailto(c)" class="btn btn-pequeno btn-primario"><Reply :size="16" /> Responder por email</a>
          <button class="btn btn-pequeno" @click="resolver(c)"><Check :size="16" /> Marcar como resuelta</button>
        </div>
      </li>
    </ul>
    <EstadoVacio v-else :icono="Inbox" titulo="Bandeja vacía" texto="No hay consultas pendientes. ¡Buen trabajo!" />
  </div>
</template>

<style scoped>
.lista {
  display: grid;
  gap: 1rem;
}

.meta {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: baseline;
}

blockquote {
  margin: 0.75rem 0 1rem;
  padding: 0.75rem 1rem;
  background: var(--crema);
  border-left: 3px solid var(--naranja);
  border-radius: 0 var(--radio-sm) var(--radio-sm) 0;
  white-space: pre-line;
}
</style>
