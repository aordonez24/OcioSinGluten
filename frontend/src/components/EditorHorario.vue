<script setup>
import { computed } from 'vue'
import { Clock, Plus, X } from 'lucide-vue-next'
import { DIAS } from '@/horario'

// null: horario desconocido. Si no, 7 listas (lunes a domingo) de franjas { abre, cierra }.
const horario = defineModel({ type: Array, default: null })
defineProps({ error: { type: String, default: '' } })

const franjaVacia = () => ({ abre: '', cierra: '' })
const primerDiaAbierto = computed(() => horario.value?.findIndex((franjas) => franjas.length) ?? -1)

function empezar() {
  horario.value = DIAS.map(() => [franjaVacia()])
}

function alternarDia(dia) {
  horario.value[dia] = horario.value[dia].length ? [] : [franjaVacia()]
}

function repetirPrimerDia() {
  const origen = horario.value[primerDiaAbierto.value]
  horario.value = horario.value.map((franjas) => (franjas.length ? origen.map((f) => ({ ...f })) : []))
}
</script>

<template>
  <fieldset class="editor-horario">
    <legend>Horario</legend>

    <template v-if="!horario">
      <p class="ayuda">Con el horario, quien busque podrá saber si está abierto ahora mismo.</p>
      <div><button type="button" class="btn btn-pequeno" @click="empezar"><Clock :size="16" /> Añadir el horario</button></div>
    </template>

    <template v-else>
      <p class="ayuda">Desmarca los días que cierra. Si cierra de madrugada, pon la hora aunque sea del día siguiente.</p>
      <ul class="lista-limpia dias">
        <li v-for="(franjas, d) in horario" :key="d">
          <label class="casilla dia">
            <input type="checkbox" :checked="franjas.length > 0" @change="alternarDia(d)" />
            {{ DIAS[d] }}
          </label>
          <div v-if="franjas.length" class="franjas">
            <div v-for="(f, i) in franjas" :key="i" class="franja">
              <input v-model="f.abre" type="time" :aria-label="`${DIAS[d]}: abre a las`" :aria-invalid="!!error && !f.abre" />
              <span aria-hidden="true">a</span>
              <input v-model="f.cierra" type="time" :aria-label="`${DIAS[d]}: cierra a las`" :aria-invalid="!!error && !f.cierra" />
              <button
                v-if="franjas.length > 1"
                type="button"
                class="btn btn-pequeno btn-icono btn-fantasma"
                :aria-label="`Quitar la franja ${i + 1} del ${DIAS[d].toLowerCase()}`"
                @click="franjas.splice(i, 1)"
              >
                <X :size="18" />
              </button>
            </div>
            <button
              v-if="franjas.length < 3"
              type="button"
              class="enlace otra"
              :aria-label="`Añadir otra franja al ${DIAS[d].toLowerCase()}`"
              @click="franjas.push(franjaVacia())"
            >
              <Plus :size="14" aria-hidden="true" /> Otra franja
            </button>
          </div>
          <span v-else class="suave cerrado">Cerrado</span>
        </li>
      </ul>
      <span class="error-campo" role="alert">{{ error }}</span>
      <div class="fila">
        <button v-if="primerDiaAbierto >= 0" type="button" class="btn btn-pequeno" @click="repetirPrimerDia">
          Repetir el {{ DIAS[primerDiaAbierto].toLowerCase() }} en los demás días
        </button>
        <button type="button" class="enlace" @click="horario = null">Quitar el horario</button>
      </div>
    </template>
  </fieldset>
</template>

<style scoped>
.editor-horario {
  gap: 0.75rem;
}

.editor-horario legend {
  font-size: 0.9375rem;
  font-weight: 600;
}

.editor-horario > p {
  margin: 0;
}

.dias > li {
  display: grid;
  grid-template-columns: 8.5rem minmax(0, 1fr);
  gap: 0.5rem 1rem;
  align-items: start;
  padding: 0.6rem 0;
  border-top: 1px solid var(--borde);
}

.dias > li:last-child {
  border-bottom: 1px solid var(--borde);
}

.dia {
  min-height: 44px;
  font-weight: 600;
}

.cerrado {
  align-self: center;
  font-size: 0.9375rem;
}

.franjas {
  display: grid;
  gap: 0.4rem;
  justify-items: start;
}

.franja {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.franja input {
  width: 8rem;
}

.franja .btn,
.franja .btn svg {
  flex-shrink: 0;
}

.franja .btn {
  color: var(--texto-suave);
}

.otra {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  min-height: 32px;
  font-size: 0.875rem;
  font-weight: 600;
}

@media (max-width: 520px) {
  .dias > li {
    grid-template-columns: minmax(0, 1fr);
  }

  .franjas {
    padding-left: 1.85rem;
  }

  .cerrado {
    display: none;
  }

  .franja input {
    width: 7.25rem;
  }
}
</style>
