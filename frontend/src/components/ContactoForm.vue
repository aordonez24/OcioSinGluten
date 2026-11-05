<script setup>
// Antes este formulario (y su lógica) estaba copiado en casi todas las vistas.
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
  <section id="contacto" class="tarjeta">
    <h2>¿Tienes alguna duda o sugerencia?</h2>
    <p v-if="enviado">¡Gracias! Hemos recibido tu mensaje y te responderemos por email.</p>
    <form v-else class="formulario" @submit.prevent="enviar">
      <div class="dos-columnas">
        <label>Nombre <input v-model="datos.nombre" required maxlength="60" /></label>
        <label>Email <input v-model="datos.email" type="email" required /></label>
      </div>
      <label>Mensaje <textarea v-model="datos.mensaje" rows="4" required maxlength="1000"></textarea></label>
      <div>
        <button class="btn btn-primario" :disabled="enviando">Enviar mensaje</button>
      </div>
    </form>
  </section>
</template>
