<script setup>
// Formulario de contacto (en el proyecto original estaba copiado en casi todas las vistas).
import { reactive, ref } from 'vue'
import { api } from '@/api'
import { useAvisos } from '@/stores/avisos'

const avisos = useAvisos()
const datos = reactive({ nombre: '', email: '', mensaje: '' })
const enviando = ref(false)
const enviado = ref(false)

async function enviar() {
  enviando.value = true
  try {
    await api('/consultas', { method: 'POST', body: datos })
    enviado.value = true
  } catch (e) {
    avisos.error(e)
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <section id="contacto" class="contacto" aria-labelledby="titulo-contacto">
    <div class="texto">
      <h2 id="titulo-contacto">Contacto</h2>
      <p>¿Tienes alguna duda o sugerencia, o conoces un sitio que debería estar aquí? Escríbenos y te respondemos por email.</p>
    </div>
    <p v-if="enviado" class="enviado" role="status">Gracias, hemos recibido tu mensaje. Te responderemos por email.</p>
    <form v-else class="formulario" @submit.prevent="enviar">
      <div class="dos-columnas">
        <label>Nombre <input v-model="datos.nombre" required maxlength="60" autocomplete="name" /></label>
        <label>Email <input v-model="datos.email" type="email" required autocomplete="email" /></label>
      </div>
      <label>Mensaje <textarea v-model="datos.mensaje" rows="4" required maxlength="1000"></textarea></label>
      <div>
        <button class="btn btn-primario" :disabled="enviando">{{ enviando ? 'Enviando…' : 'Enviar mensaje' }}</button>
      </div>
    </form>
  </section>
</template>

<style scoped>
.contacto {
  display: grid;
  gap: 1.5rem 4rem;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1.6fr);
  align-items: start;
}

.texto p {
  margin: 0;
  color: var(--texto-suave);
  max-width: 34em;
}

.enviado {
  margin: 0;
  padding: 1rem 1.25rem;
  background: var(--verde-claro);
  color: var(--verde);
  border-radius: var(--radio-sm);
  font-weight: 500;
}

@media (max-width: 800px) {
  .contacto {
    grid-template-columns: 1fr;
  }
}
</style>
