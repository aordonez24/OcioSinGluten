<script setup>
import { computed, reactive, ref } from 'vue'
import { NOMBRES_PROVINCIAS, PROVINCIAS } from '@/provincias'
import { validar } from '@/utils'
import { CATEGORIAS, DISTINTIVOS, PRECIOS } from '@/catalogo'

const props = defineProps({
  inicial: { type: Object, default: () => ({}) },
  textoBoton: { type: String, default: 'Guardar' },
  enviando: { type: Boolean, default: false },
  error: { type: String, default: '' },
  cancelable: { type: Boolean, default: false },
})
const emit = defineEmits(['guardar', 'cancelar'])

const CAMPOS = ['nombre', 'telefono', 'calle', 'cod_postal', 'localidad', 'provincia', 'pais']
const datos = reactive({
  ...Object.fromEntries(CAMPOS.map((c) => [c, String(props.inicial[c] ?? (c === 'pais' ? 'España' : ''))])),
  categoria: props.inicial.categoria ?? 'restaurante',
  precio: props.inicial.precio ?? '',
  horario: props.inicial.horario ?? '',
  web: props.inicial.web ?? '',
  ...Object.fromEntries(DISTINTIVOS.map((d) => [d.clave, Boolean(props.inicial[d.clave])])),
})
const intentado = ref(false)

const errores = computed(() => ({
  nombre: datos.nombre.trim() ? '' : 'Obligatorio',
  telefono: validar.telefono(datos.telefono),
  calle: datos.calle.trim() ? '' : 'Obligatorio',
  cod_postal: validar.codPostal(datos.cod_postal),
  localidad: datos.localidad.trim() ? '' : 'Obligatorio',
  provincia: datos.provincia.trim() ? '' : 'Obligatorio',
  pais: datos.pais.trim() ? '' : 'Obligatorio',
  web: !datos.web.trim() || /^(https?:\/\/)?[^\s/$.?#][^\s]*\.[^\s]{2,}$/i.test(datos.web.trim()) ? '' : 'No parece una dirección web',
}))
const editando = Boolean(props.inicial.id)
const esEspana = computed(() => datos.pais.trim().toLowerCase() === 'españa')
const localidades = computed(() => PROVINCIAS[datos.provincia] ?? [])
const err = (campo) => (intentado.value ? errores.value[campo] : '')

function enviar() {
  intentado.value = true
  if (Object.values(errores.value).some(Boolean)) return
  // Al crear, lo que no se rellena no se envía; al editar, vacío significa "quitar el dato".
  const vacio = (valor, borrar) => valor || (editando ? borrar : null)
  emit('guardar', {
    ...datos,
    precio: vacio(Number(datos.precio), 0),
    horario: vacio(datos.horario.trim(), ''),
    web: vacio(datos.web.trim(), ''),
  })
}
</script>

<template>
  <form class="formulario" novalidate @submit.prevent="enviar">
    <label>
      Nombre del establecimiento
      <input v-model="datos.nombre" maxlength="60" :aria-invalid="!!err('nombre')" />
      <span class="error-campo">{{ err('nombre') }}</span>
    </label>

    <label>
      Tipo de sitio
      <select v-model="datos.categoria">
        <option v-for="(c, clave) in CATEGORIAS" :key="clave" :value="clave">{{ c.texto }}</option>
      </select>
    </label>

    <fieldset class="distintivos">
      <legend>¿Qué ofrece para personas celíacas? <span class="ayuda">Marca solo lo que sepas con seguridad.</span></legend>
      <label v-for="d in DISTINTIVOS" :key="d.clave" class="distintivo" :class="{ marcado: datos[d.clave] }">
        <input v-model="datos[d.clave]" type="checkbox" />
        <component :is="d.icono" :size="20" />
        <span>
          <strong>{{ d.texto }}</strong>
          <span class="ayuda">{{ d.descripcion }}</span>
        </span>
      </label>
    </fieldset>

    <fieldset class="practica">
      <legend>Información práctica <span class="ayuda">Opcional</span></legend>
      <div class="dos-columnas">
        <label>
          Precio
          <select v-model="datos.precio">
            <option value="">Sin indicar</option>
            <option v-for="(p, n) in PRECIOS" :key="n" :value="Number(n)">{{ p.simbolo }} {{ p.texto }}</option>
          </select>
        </label>
        <label>
          Web
          <input v-model="datos.web" inputmode="url" maxlength="200" placeholder="www.ejemplo.es" :aria-invalid="!!err('web')" />
          <span class="error-campo">{{ err('web') }}</span>
        </label>
      </div>
      <label>
        Horario
        <textarea v-model="datos.horario" rows="2" maxlength="300" placeholder="De martes a domingo, de 13:00 a 16:30 y de 20:30 a 23:30"></textarea>
      </label>
    </fieldset>

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

<style scoped>
.distintivos {
  display: grid;
  gap: 0.6rem;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
}

.practica legend {
  font-size: 0.9375rem;
  font-weight: 600;
  display: flex;
  gap: 0.5rem;
  align-items: baseline;
}

.distintivos legend {
  font-size: 0.9375rem;
  font-weight: 600;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: baseline;
}

.distintivo {
  display: flex;
  align-items: flex-start;
  gap: 0.6rem;
  padding: 0.7rem 0.8rem;
  border: 1px solid var(--borde-fuerte);
  border-radius: var(--radio-sm);
  background: #fff;
  cursor: pointer;
  font-weight: 500;
}

.distintivo input {
  width: 20px;
  height: 20px;
  min-height: 0;
  margin-top: 1px;
  accent-color: var(--verde);
}

.distintivo svg {
  flex-shrink: 0;
  color: var(--texto-suave);
}

.distintivo > span {
  display: grid;
  line-height: 1.3;
}

.distintivo.marcado {
  border-color: var(--verde);
  background: var(--verde-claro);
}

.distintivo.marcado svg {
  color: var(--verde);
}
</style>
