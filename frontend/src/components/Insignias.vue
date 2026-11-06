<script setup>
// Insignias del perfil. En el perfil propio se ven todas, con lo que falta para cada una; en el de
// otra persona, solo las que ha conseguido (no se enseña lo que le falta a nadie).
import { computed, ref } from 'vue'
import { Camera, ChevronDown, Map, MapPinPlus, Megaphone, MessageSquareText, ThumbsUp, Users } from 'lucide-vue-next'

const props = defineProps({
  insignias: { type: Array, required: true },
  propio: { type: Boolean, default: false },
})

const ICONOS = {
  primera_opinion: MessageSquareText,
  abre_camino: MapPinPlus,
  trotamundos: Map,
  buen_ojo: Camera,
  ayuda: ThumbsUp,
  voz: Megaphone,
  compania: Users,
}

const conseguidas = computed(() => props.insignias.filter((i) => i.conseguida).length)

// Con muchas, se enseñan 4 (las conseguidas van primero) para no alejar las pestañas del perfil.
const VISIBLES = 4
const todas = ref(false)
const mostradas = computed(() => (todas.value ? props.insignias : props.insignias.slice(0, VISIBLES)))
</script>

<template>
  <section v-if="insignias.length" class="insignias" aria-labelledby="titulo-insignias">
    <div class="encabezado">
      <h2 id="titulo-insignias">Insignias</h2>
      <p v-if="propio" class="suave pequeno">Llevas {{ conseguidas }} de {{ insignias.length }}</p>
    </div>
    <ul class="lista-limpia rejilla-insignias">
      <li v-for="i in mostradas" :key="i.clave" :class="{ conseguida: i.conseguida }">
        <span class="icono" aria-hidden="true"><component :is="ICONOS[i.clave]" :size="22" /></span>
        <div>
          <strong>{{ i.titulo }}</strong>
          <p class="pequeno">{{ i.descripcion }}</p>
          <template v-if="!i.conseguida">
            <span class="progreso" aria-hidden="true"><span :style="{ width: `${(i.actual / i.meta) * 100}%` }"></span></span>
            <p class="pequeno cuenta">{{ i.actual }} de {{ i.meta }}</p>
          </template>
        </div>
      </li>
    </ul>
    <button v-if="insignias.length > VISIBLES" class="enlace ver-todas" :aria-expanded="todas" @click="todas = !todas">
      {{ todas ? 'Ver menos' : `Ver las ${insignias.length} insignias` }}
      <ChevronDown :size="16" aria-hidden="true" :class="{ girado: todas }" />
    </button>
  </section>
</template>

<style scoped>
.insignias {
  margin: 2rem 0;
}

.encabezado {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 0.25rem 1rem;
  margin-bottom: 0.75rem;
}

.encabezado h2 {
  font-size: 1.125rem;
  margin: 0;
}

.encabezado p {
  margin: 0;
}

.rejilla-insignias {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 0.5rem 1.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid var(--borde);
}

.rejilla-insignias li {
  display: flex;
  gap: 0.85rem;
  align-items: flex-start;
  padding: 0.6rem 0;
}

.ver-todas {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  min-height: 44px;
  font-weight: 600;
}

.ver-todas svg {
  transition: rotate 0.2s ease;
}

.ver-todas .girado {
  rotate: 180deg;
}

/* En el móvil, una por fila con líneas finas entre ellas, como el resto de listas. */
@media (max-width: 560px) {
  .rejilla-insignias {
    gap: 0;
    padding-top: 0;
  }

  .rejilla-insignias li {
    padding: 0.85rem 0;
  }

  .rejilla-insignias li + li {
    border-top: 1px solid var(--borde);
  }
}

.icono {
  flex-shrink: 0;
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--niebla);
  color: var(--texto-suave);
}

.conseguida .icono {
  background: var(--turquesa);
  color: var(--carbon);
}

.rejilla-insignias strong {
  display: block;
  font-weight: 600;
  color: var(--carbon);
}

li:not(.conseguida) strong {
  color: var(--texto-suave);
}

.rejilla-insignias p {
  margin: 0.1rem 0 0;
  color: var(--texto-suave);
}

.progreso {
  display: block;
  width: 120px;
  height: 6px;
  margin-top: 0.5rem;
  border-radius: 3px;
  background: var(--niebla-2);
  overflow: hidden;
}

.progreso span {
  display: block;
  height: 100%;
  background: var(--turquesa-oscuro);
}

.cuenta {
  font-variant-numeric: tabular-nums;
}
</style>
