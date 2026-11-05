<script setup>
import { UserCheck, UserPlus } from 'lucide-vue-next'
import Avatar from '@/components/Avatar.vue'

defineProps({
  usuarios: { type: Array, required: true },
  // Si se activa, muestra el botón Seguir (requiere `siguiendo` en cada usuario).
  seguir: { type: Boolean, default: false },
})
const emit = defineEmits(['alternar-seguir'])
</script>

<template>
  <ul class="lista-limpia rejilla-usuarios">
    <li v-for="u in usuarios" :key="u.username" class="usuario">
      <RouterLink :to="`/u/${u.username}`" class="enlace">
        <Avatar :usuario="u" :tamano="52" />
        <span class="texto">
          <strong>{{ u.nombre }}</strong>
          <span class="suave pequeno">@{{ u.username }}</span>
          <span v-if="u.num_seguidores !== undefined" class="suave pequeno">
            {{ u.num_seguidores }} {{ u.num_seguidores === 1 ? 'seguidor' : 'seguidores' }}
          </span>
        </span>
      </RouterLink>
      <span v-if="u.archivado" class="chip peligro">Suspendido</span>
      <button
        v-else-if="seguir"
        class="btn btn-pequeno"
        :class="{ 'btn-primario': !u.siguiendo }"
        :aria-pressed="u.siguiendo"
        @click="emit('alternar-seguir', u)"
      >
        <component :is="u.siguiendo ? UserCheck : UserPlus" :size="15" />
        {{ u.siguiendo ? 'Siguiendo' : 'Seguir' }}
      </button>
    </li>
  </ul>
</template>

<style scoped>
.rejilla-usuarios {
  display: grid;
  column-gap: 2.5rem;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
}

.usuario {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-block: 0.9rem;
  border-bottom: 1px solid var(--borde);
}

.enlace {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  color: inherit;
  text-decoration: none;
}

.texto {
  display: grid;
  min-width: 0;
  line-height: 1.3;
}

.texto > * {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.enlace:hover strong {
  text-decoration: underline;
  text-underline-offset: 0.15em;
}
</style>
