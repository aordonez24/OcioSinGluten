<template>
  <cabecera-componente />
  <Vista-Inicio />
</template>

<script>
import CabeceraComponente from "@/components/header.vue";
import VistaInicio from "@/views/Inicio.vue"

export default {
  name: 'App',
  components: {
    CabeceraComponente,
    VistaInicio
  }
}
</script>
