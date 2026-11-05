<script setup>
// "Avisar de un error": cualquier miembro puede decir al equipo de administración que un sitio
// ha cerrado, ya no tiene opciones sin gluten o tiene algún dato mal. Para una persona celíaca
// un dato desactualizado puede significar ponerse enferma, así que el aviso está a mano.
import { nextTick, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Flag } from 'lucide-vue-next'
import { api } from '@/api'
import { MOTIVOS_AVISO } from '@/catalogo'
import { useAuth } from '@/stores/auth'
import { useAvisos } from '@/stores/avisos'

const props = defineProps({ estId: { type: Number, required: true } })

const auth = useAuth()
const avisos = useAvisos()
const route = useRoute()
const router = useRouter()
const dialogo = ref(null)
const datos = reactive({ motivo: '', mensaje: '' })
const enviando = ref(false)
const error = ref('')

async function abrir() {
  if (!auth.autenticado) {
    avisos.error('Inicia sesión para avisar de un error')
    return router.push({ name: 'login', query: { volver: route.fullPath } })
  }
  Object.assign(datos, { motivo: '', mensaje: '' })
  error.value = ''
  dialogo.value.showModal()
  await nextTick()
  dialogo.value.querySelector('input')?.focus()
}

async function enviar() {
  if (!datos.motivo) return (error.value = 'Elige qué pasa con este sitio')
  enviando.value = true
  try {
    await api(`/establecimientos/${props.estId}/avisos`, { method: 'POST', body: datos })
    dialogo.value.close()
    avisos.ok('Gracias por avisar. El equipo de administración lo revisará.')
  } catch (e) {
    error.value = e.message
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <button type="button" class="enlace avisar" @click="abrir"><Flag :size="15" aria-hidden="true" /> Avisar de un error</button>

  <dialog ref="dialogo" class="dialogo" aria-labelledby="titulo-aviso" @click.self="dialogo.close()">
    <form class="formulario" @submit.prevent="enviar">
      <h2 id="titulo-aviso">Avisar de un error</h2>
      <fieldset>
        <legend class="solo-lectores">¿Qué pasa con este sitio?</legend>
        <label v-for="(m, clave) in MOTIVOS_AVISO" :key="clave" class="casilla">
          <input v-model="datos.motivo" type="radio" name="motivo" :value="clave" />
          {{ m.opcion }}
        </label>
      </fieldset>
      <label>
        <span>Detalles <span class="ayuda">(opcional)</span></span>
        <textarea v-model="datos.mensaje" rows="3" maxlength="1000" placeholder="Por ejemplo, el horario nuevo o el teléfono correcto"></textarea>
      </label>
      <p v-if="error" class="alerta" role="alert">{{ error }}</p>
      <div class="fila acciones">
        <button type="button" class="btn" @click="dialogo.close()">Cancelar</button>
        <button class="btn btn-primario" :disabled="enviando">{{ enviando ? 'Enviando…' : 'Enviar aviso' }}</button>
      </div>
    </form>
  </dialog>
</template>

<style scoped>
.avisar {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  min-height: 44px;
  color: var(--texto-suave);
  font-size: 0.875rem;
}

.avisar:hover {
  color: var(--carbon);
}

.dialogo {
  border: 0;
  border-radius: var(--radio);
  padding: 1.5rem;
  width: min(480px, 100% - 2rem);
  box-shadow: 0 16px 48px rgb(0 0 0 / 22%);
  color: var(--texto);
}

.dialogo::backdrop {
  background: rgb(53 53 53 / 55%);
}

h2 {
  font-size: 1.25rem;
  margin: 0;
}

fieldset {
  gap: 0.25rem;
}

.casilla {
  min-height: 40px;
}

.acciones {
  justify-content: flex-end;
}
</style>
