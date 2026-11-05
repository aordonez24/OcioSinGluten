<script setup>
import { computed, reactive, ref } from 'vue'
import { NOMBRES_PROVINCIAS, PROVINCIAS } from '@/provincias'
import { validar } from '@/utils'

const props = defineProps({
  inicial: { type: Object, default: () => ({}) },
  textoBoton: { type: String, default: 'Guardar' },
  enviando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  cancelable: { type: Boolean, default: false },
})
const emit = defineEmits(['guardar', 'cancelar'])

const CAMPOS = ['nombre', 'telefono', 'calle', 'cod_postal', 'localidad', 'provincia', 'pais']
const datos = reactive(
  Object.fromEntries(CAMPOS.map((c) => [c, props.inicial[c] ?? (c === 'pais' ? 'España' : '')])),
)
const intentado = ref(false)

const errores = computed(() => ({
  nombre: datos.nombre.trim() ? '' : 'Obligatorio',
  telefono: validar.telefono(datos.telefono),
  calle: datos.calle.trim() ? '' : 'Obligatorio',
  cod_postal: validar.codPostal(datos.cod_postal),
  localidad: datos.localidad.trim() ? '' : 'Obligatorio',
  provincia: datos.provincia.trim() ? '' : 'Obligatorio',
  pais: datos.pais.trim() ? '' : 'Obligatorio',
}))
const esEspana = computed(() => datos.pais.trim().toLowerCase() === 'españa')
const localidades = computed(() => PROVINCIAS[datos.provincia] ?? [])
const err = (campo) => (intentado.value ? errores.value[campo] : '')

function enviar() {
  intentado.value = true
  if (Object.values(errores.value).some(Boolean)) return
  emit('guardar', { ...datos })
}
</script>

<template>
  <form class="formulario" novalidate @submit.prevent="enviar">
    <label>
      Nombre del establecimiento
      <input v-model="datos.nombre" maxlength="60" :aria-invalid="!!err('nombre')" />
      <span class="error-campo">{{ err('nombre') }}</span>
    </label>

    <div class="dos-columnas">
      <label>
        Teléfono
        <input v-model="datos.telefono" inputmode="tel" :aria-invalid="!!err('telefono')" />
        <span class="error-campo">{{ err('telefono') }}</span>
      </label>
      <label>
        País
        <input v-model="datos.pais" maxlength="60" :aria-invalid="!!err('pais')" />
        <span class="error-campo">{{ err('pais') }}</span>
      </label>
    </div>

    <div class="dos-columnas">
      <label>
        Provincia
        <select v-if="esEspana" v-model="datos.provincia" :aria-invalid="!!err('provincia')">
          <option value="" disabled>Elige una provincia</option>
          <option v-for="p in NOMBRES_PROVINCIAS" :key="p">{{ p }}</option>
        </select>
        <input v-else v-model="datos.provincia" maxlength="60" :aria-invalid="!!err('provincia')" />
        <span class="error-campo">{{ err('provincia') }}</span>
      </label>
      <label>
        Localidad
        <input v-model="datos.localidad" list="localidades" maxlength="60" :aria-invalid="!!err('localidad')" />
        <datalist id="localidades">
          <option v-for="l in localidades" :key="l" :value="l" />
        </datalist>
        <span class="error-campo">{{ err('localidad') }}</span>
      </label>
    </div>

    <div class="dos-columnas">
      <label>
        Dirección
        <input v-model="datos.calle" maxlength="120" placeholder="Calle y número" :aria-invalid="!!err('calle')" />
        <span class="error-campo">{{ err('calle') }}</span>
      </label>
      <label>
        Código postal
        <input v-model="datos.cod_postal" inputmode="numeric" maxlength="5" :aria-invalid="!!err('cod_postal')" />
        <span class="error-campo">{{ err('cod_postal') }}</span>
      </label>
    </div>

    <p v-if="error" class="alerta">{{ error }}</p>

    <div class="fila">
      <button class="btn btn-primario" :disabled="enviando">{{ enviando ? 'Guardando…' : textoBoton }}</button>
      <button v-if="cancelable" type="button" class="btn" @click="emit('cancelar')">Cancelar</button>
    </div>
  </form>
</template>
