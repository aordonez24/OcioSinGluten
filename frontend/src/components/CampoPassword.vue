<script setup>
import { computed, ref } from 'vue'
import { Check, Eye, EyeOff, Minus } from 'lucide-vue-next'

const modelo = defineModel({ type: String, default: '' })
defineProps({
  autocomplete: { type: String, default: 'current-password' },
  invalido: { type: Boolean, default: false },
  medidor: { type: Boolean, default: false },
})
defineOptions({ inheritAttrs: false })

const visible = ref(false)

const REQUISITOS = [
  { texto: '8 caracteres', ok: (v) => v.length >= 8 },
  { texto: 'Una mayúscula', ok: (v) => /[A-Z]/.test(v) },
  { texto: 'Un número', ok: (v) => /\d/.test(v) },
]
const cumplidos = computed(() => REQUISITOS.map((r) => r.ok(modelo.value)))
const fuerza = computed(() => cumplidos.value.filter(Boolean).length + (modelo.value.length >= 12 ? 1 : 0))
</script>

<template>
  <span class="campo">
    <input
      v-model="modelo"
      v-bind="$attrs"
      :type="visible ? 'text' : 'password'"
      :autocomplete="autocomplete"
      :aria-invalid="invalido"
    />
    <button type="button" class="ojo" :aria-pressed="visible" @click="visible = !visible">
      <component :is="visible ? EyeOff : Eye" :size="18" />
      <span class="solo-lectores">{{ visible ? 'Ocultar' : 'Mostrar' }} contraseña</span>
    </button>
  </span>
  <span v-if="medidor" class="medidor" aria-live="polite">
    <span class="barras" :data-fuerza="fuerza"><i></i><i></i><i></i><i></i></span>
    <span v-for="(r, i) in REQUISITOS" :key="r.texto" class="requisito" :class="{ ok: cumplidos[i] }">
      <component :is="cumplidos[i] ? Check : Minus" :size="14" aria-hidden="true" /> {{ r.texto }}
      <span class="solo-lectores">{{ cumplidos[i] ? '(cumplido)' : '(pendiente)' }}</span>
    </span>
  </span>
</template>

<style scoped>
.campo {
  position: relative;
  display: block;
}

.campo input {
  padding-right: 2.8rem;
}

.ojo {
  position: absolute;
  right: 0.2rem;
  top: 50%;
  translate: 0 -50%;
  border: 0;
  background: none;
  color: var(--texto-suave);
  width: 40px;
  height: 40px;
  border-radius: var(--radio-sm);
  cursor: pointer;
  display: grid;
  place-items: center;
}

.ojo:hover {
  color: var(--texto);
}

.medidor {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.25rem 0.75rem;
  font-size: 0.78rem;
  font-weight: 500;
}

.barras {
  display: flex;
  gap: 3px;
  width: 100%;
}

.barras i {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  background: var(--niebla-2);
  transition: background 0.2s;
}

.barras[data-fuerza='1'] i:nth-child(-n + 1),
.barras[data-fuerza='2'] i:nth-child(-n + 2) {
  background: var(--estrella);
}

.barras[data-fuerza='3'] i:nth-child(-n + 3),
.barras[data-fuerza='4'] i {
  background: var(--verde);
}

.requisito {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  color: var(--texto-suave);
}

.requisito.ok {
  color: var(--verde);
}
</style>
