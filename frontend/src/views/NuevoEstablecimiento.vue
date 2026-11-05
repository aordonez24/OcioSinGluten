<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Lightbulb } from 'lucide-vue-next'
import { api } from '@/api'
import { useAvisos } from '@/stores/avisos'
import EstablecimientoForm from '@/components/EstablecimientoForm.vue'

const router = useRouter()
const avisos = useAvisos()
const enviando = ref(false)
const error = ref('')

async function crear(datos) {
  enviando.value = true
  error.value = ''
  try {
    const est = await api('/establecimientos', { method: 'POST', body: datos })
    avisos.ok('¡Publicado! Ahora puedes añadir fotos y tu opinión.')
    router.push(`/establecimientos/${est.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="contenedor">
    <RouterLink to="/establecimientos" class="volver"><ArrowLeft :size="16" /> Explorar</RouterLink>
    <div class="disposicion">
      <div class="tarjeta">
        <h1>Añadir un sitio sin gluten</h1>
        <p class="suave">Comparte un restaurante, bar o cafetería que aún no esté en Ocio Sin Gluten.</p>
        <EstablecimientoForm texto-boton="Publicar establecimiento" :enviando="enviando" :error="error" @guardar="crear" />
      </div>
      <aside class="tarjeta consejos">
        <h2><Lightbulb :size="20" /> Consejos</h2>
        <ul>
          <li>Comprueba en <RouterLink to="/establecimientos">Explorar</RouterLink> que el sitio no esté ya añadido.</li>
          <li>Escribe la dirección completa: la usamos para situarlo en el mapa.</li>
          <li>Después podrás subir fotos de la carta o de los platos sin gluten.</li>
          <li>Cuenta en tu opinión si tienen cocina separada o protocolo contra la contaminación cruzada.</li>
        </ul>
      </aside>
    </div>
  </div>
</template>

<style scoped>
.disposicion {
  display: grid;
  gap: 1.5rem;
  grid-template-columns: minmax(0, 1fr) 300px;
  align-items: start;
}

.consejos {
  background: #fff8ef;
}

.consejos h2 {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 1.05rem;
}

.consejos ul {
  margin: 0;
  padding-left: 1.1rem;
  display: grid;
  gap: 0.6rem;
  color: var(--texto-suave);
  font-size: 0.92rem;
}

@media (max-width: 860px) {
  .disposicion {
    grid-template-columns: 1fr;
  }
}
</style>
