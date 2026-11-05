<script setup>
// Navegación inferior en móvil: las secciones principales siempre a un toque.
import { computed } from 'vue'
import { Compass, House, MessagesSquare, Activity, UserRound, LogIn, Plus } from 'lucide-vue-next'
import { useAuth } from '@/stores/auth'

const auth = useAuth()
const enlaces = computed(() =>
  auth.autenticado
    ? [
        { to: '/', icono: House, texto: 'Inicio', exacto: true },
        { to: '/establecimientos', icono: Compass, texto: 'Explorar', exacto: true },
        { to: '/establecimientos/nuevo', icono: Plus, texto: 'Añadir', destacado: true },
        { to: '/chat', icono: MessagesSquare, texto: 'Chat' },
        { to: `/u/${auth.usuario.username}`, icono: UserRound, texto: 'Perfil' },
      ]
    : [
        { to: '/', icono: House, texto: 'Inicio', exacto: true },
        { to: '/establecimientos', icono: Compass, texto: 'Explorar' },
        { to: '/actividad', icono: Activity, texto: 'Actividad' },
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
      :class="{ destacado: e.destacado }"
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
    height: var(--alto-barra-movil);
    background: var(--blanco);
    border-top: 1px solid var(--borde);
    box-shadow: 0 -4px 16px rgb(43 29 18 / 6%);
    z-index: 100;
    padding-bottom: env(safe-area-inset-bottom);
  }

  a {
    flex: 1;
    display: grid;
    justify-items: center;
    align-content: center;
    gap: 2px;
    font-size: 0.7rem;
    font-weight: 600;
    color: var(--texto-suave);
    text-decoration: none;
  }

  a.activo {
    color: var(--coral-oscuro);
  }

  .destacado .icono {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    margin-top: -14px;
    border-radius: 50%;
    background: var(--coral);
    color: #fff;
    box-shadow: 0 4px 12px rgb(226 96 74 / 40%);
  }
}
</style>
