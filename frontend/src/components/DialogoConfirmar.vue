<script setup>
import { nextTick, ref, watch } from 'vue'
import { useConfirmar } from '@/stores/confirmar'

const confirmar = useConfirmar()
const dialogo = ref(null)
const botonAceptar = ref(null)

watch(
  () => confirmar.abierto,
  async (abierto) => {
    if (abierto) {
      dialogo.value.showModal()
      await nextTick()
      botonAceptar.value?.focus()
    } else if (dialogo.value?.open) {
      dialogo.value.close()
    }
  },
)
</script>

<template>
  <dialog
    ref="dialogo"
    class="dialogo"
    aria-labelledby="dialogo-titulo"
    @cancel.prevent="confirmar.responder(false)"
    @click.self="confirmar.responder(false)"
  >
    <h2 id="dialogo-titulo">{{ confirmar.opciones.titulo }}</h2>
    <p v-if="confirmar.opciones.texto" class="suave">{{ confirmar.opciones.texto }}</p>
    <div class="fila acciones">
      <button class="btn" @click="confirmar.responder(false)">Cancelar</button>
      <button
        ref="botonAceptar"
        class="btn"
        :class="confirmar.opciones.peligro ? 'btn-peligro' : 'btn-primario'"
        @click="confirmar.responder(true)"
      >
        {{ confirmar.opciones.accion }}
      </button>
    </div>
  </dialog>
</template>

<style scoped>
.dialogo {
  border: 0;
  border-radius: var(--radio);
  padding: 1.5rem;
  width: min(420px, 100% - 2rem);
  box-shadow: 0 16px 48px rgb(0 0 0 / 22%);
  background: var(--blanco);
  color: var(--texto);
}

.dialogo::backdrop {
  background: rgb(53 53 53 / 55%);
}

h2 {
  font-size: 1.2rem;
}

.acciones {
  justify-content: flex-end;
  margin-top: 1.25rem;
}
</style>
