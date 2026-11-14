<script setup>
import { Download, Smartphone } from 'lucide-vue-next'

const esIphone = /iPhone|iPad|iPod/.test(navigator.userAgent)
</script>

<template>
  <article class="contenedor medio texto">
    <h1>La app de Ocio Sin Gluten</h1>
    <p class="entradilla">
      Los mismos sitios, opiniones y chat que en la web, con el GPS del móvil para encontrar lo que tienes
      cerca, la hoja de compartir del teléfono y uso sin conexión.
    </p>

    <section :class="{ secundaria: esIphone }" aria-labelledby="titulo-android">
      <h2 id="titulo-android">Android</h2>
      <p>
        <a href="/descargas/ociosingluten.apk" class="btn btn-primario" download>
          <Download :size="18" aria-hidden="true" /> Descargar para Android
        </a>
      </p>
      <p class="suave">
        Abre el fichero descargado e instálalo. La primera vez, Android te pedirá permiso para instalar
        apps desde el navegador: concédelo solo para esta instalación. Necesitas Android 7 o posterior.
      </p>
    </section>

    <section aria-labelledby="titulo-iphone">
      <h2 id="titulo-iphone">iPhone</h2>
      <p>
        <Smartphone :size="18" aria-hidden="true" class="icono-linea" />
        Abre esta web en Safari, toca el botón de compartir y elige «Añadir a pantalla de inicio». Se abrirá
        a pantalla completa, con su icono, como cualquier otra app.
      </p>
    </section>
  </article>
</template>

<style scoped>
.texto {
  padding-block: 2.5rem 4rem;
  max-width: 72ch;
}

.entradilla {
  font-size: 1.125rem;
}

h2 {
  margin-top: 2.25rem;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.icono-linea {
  vertical-align: -3px;
  margin-right: 0.35rem;
  color: var(--turquesa-oscuro);
}

/* En un iPhone, Android va después */
article {
  display: flex;
  flex-direction: column;
}

.secundaria {
  order: 3;
}
</style>
