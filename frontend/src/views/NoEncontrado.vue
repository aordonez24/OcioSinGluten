<script setup>
import { MapPinOff } from 'lucide-vue-next'
import EstadoVacio from '@/components/EstadoVacio.vue'
</script>

<template>
  <div class="contenedor">
    <EstadoVacio :icono="MapPinOff" titulo="Página no encontrada" texto="Puede que el enlace esté roto o que la página se haya movido.">
      <div class="fila">
        <RouterLink to="/" class="btn">Ir al inicio</RouterLink>
        <RouterLink to="/establecimientos" class="btn btn-primario">Ver establecimientos</RouterLink>
      </div>
    </EstadoVacio>
  </div>
</template>
