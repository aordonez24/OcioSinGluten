<script setup>
// Navegación inferior en móvil: las secciones principales siempre a un toque.
import { computed } from 'vue'
import { Store, House, MessagesSquare, Activity, UserRound, LogIn, Plus } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'

const auth = useAuth()
const enlaces = computed(() =>
  auth.autenticado
    ? [
        { to: '/', icono: House, texto: 'Inicio', exacto: true },
        { to: '/establecimientos', icono: Store, texto: 'Sitios', exacto: true },
        { to: '/establecimientos/nuevo', icono: Plus, texto: 'Añadir' },
        { to: '/chat', icono: MessagesSquare, texto: 'Chat' },
        { to: `/u/${auth.usuario.username}`, icono: UserRound, texto: 'Perfil' },
      ]
    : [
        { to: '/', icono: House, texto: 'Inicio', exacto: true },
        { to: '/establecimientos', icono: Store, texto: 'Sitios' },
        { to: '/actividad', icono: Activity, texto: 'Actividades' },
        { to: '/entrar', icono: LogIn, texto: 'Entrar' },
      ],
)
</script>

<template>
  <nav class="barra" aria-label="Navegación principal">
    <RouterLink
      v-for="e in enlaces"
      :key="e.to"
      :to="e.to"
      :exact-active-class="e.exacto ? 'activo' : ''"
      :active-class="e.exacto ? '' : 'activo'"
    >
      <span class="icono"><component :is="e.icono" :size="22" /></span>
      {{ e.texto }}
    </RouterLink>
  </nav>
</template>

<style scoped>
.barra {
  display: none;
}

@media (max-width: 760px) {
  .barra {
    display: flex;
    position: fixed;
    inset: auto 0 0;
    height: calc(var(--alto-barra-movil) + env(safe-area-inset-bottom));
    background: var(--blanco);
    border-top: 1px solid var(--borde);
    z-index: 100;
    padding-bottom: env(safe-area-inset-bottom);
  }

  a {
    flex: 1;
    display: grid;
    justify-items: center;
    align-content: center;
    gap: 2px;
    font-size: 0.75rem;
    font-weight: 600;
    color: var(--texto-suave);
    text-decoration: none;
  }

  .icono {
    display: grid;
    place-items: center;
    width: 52px;
    height: 30px;
    border-radius: 999px;
    transition: background-color 0.15s;
  }

  a.activo {
    color: var(--carbon);
  }

  /* La sección actual se marca con la píldora turquesa, como en la cabecera de escritorio. */
  a.activo .icono {
    background: var(--turquesa);
  }
}
</style>
