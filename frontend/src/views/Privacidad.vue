<template>
  <article class="contenedor medio texto">
    <h1>Privacidad</h1>
    <p class="entradilla">
      Decir dónde comes sin gluten cuenta algo de tu salud. Por eso guardamos lo mínimo para que la
      comunidad funcione, no lo vendemos ni lo usamos para publicidad, y puedes llevártelo o borrarlo
      cuando quieras.
    </p>

    <h2>Qué guardamos</h2>
    <ul>
      <li>Tu cuenta: nombre de usuario, nombre, apellidos si los pones, email, foto de perfil si la subes y la contraseña cifrada.</li>
      <li>Lo que haces en la comunidad: opiniones, fotos de los sitios, sitios que añades, me gusta, favoritos, sitios visitados, a quién sigues y tus mensajes del chat.</li>
      <li>Los mensajes que nos envías por el formulario de contacto o al avisar de un error en un sitio.</li>
    </ul>
    <p>No pedimos DNI, teléfono ni fecha de nacimiento, y no registramos tu ubicación: «Cerca de mí» la usa en tu navegador solo para ordenar la lista.</p>

    <h2>Qué es público</h2>
    <p>
      Tu nombre de usuario, tu nombre y apellidos, tu foto, tus opiniones, tus sitios favoritos y visitados
      y tu actividad se ven en tu perfil. Los perfiles no aparecen en los buscadores. Tu email solo lo ves
      tú; el equipo de administración puede verlo para moderar la comunidad.
    </p>

    <h2>Dónde están tus datos</h2>
    <p>
      La aplicación funciona en servidores de Render y la base de datos está en Supabase, ambos en
      Frankfurt (Unión Europea). Los mapas se cargan de OpenStreetMap, que recibe tu dirección IP como
      cualquier web que visitas.
    </p>

    <h2>Cookies</h2>
    <p>
      No usamos cookies ni herramientas de analítica. Tu sesión se guarda en el almacenamiento de tu
      navegador y se borra al cerrar sesión.
    </p>

    <h2>Tus derechos</h2>
    <p>
      Desde <RouterLink to="/ajustes">Ajustes</RouterLink> puedes corregir tus datos, descargar todo lo que
      guardamos de ti en un fichero y eliminar tu cuenta: se borran tu perfil, tus opiniones, tus mensajes
      y tu actividad. Las fotos que subiste de los sitios se quedan en su ficha, sin tu nombre.
      Para cualquier otra petición, escríbenos desde el
      <RouterLink :to="{ path: '/', hash: '#contacto' }">formulario de contacto</RouterLink>. También puedes
      reclamar ante la Agencia Española de Protección de Datos.
    </p>
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

ul {
  padding-left: 1.25rem;
  display: grid;
  gap: 0.5rem;
}
</style>
