# Dirección de diseño de Ocio Sin Gluten

Cómo se ve y cómo se comporta la web. Antes de diseñar o cambiar una pantalla, lee este documento y las
skills `frontend-design` y `ui-ux-pro-max` de `.claude/skills/`. De `ui-ux-pro-max` se usan solo sus
reglas de experiencia de uso y accesibilidad, no su catálogo de estilos.

## Para quién y para qué

Personas celíacas que buscan dónde comer o tomar algo sin gluten, casi siempre desde el móvil y a menudo
ya en la calle. Lo primero que necesitan es encontrar un sitio cercano en el que puedan confiar y saber
qué ofrece: si es todo sin gluten, si tiene carta o cocina separada. Lo segundo, contar su experiencia.

## Identidad

Se mantiene la del proyecto original del TFG: cabecera blanca con el logo, el nombre y el lema
«Disfrute de su tiempo de ocio sin gluten»; enlaces de navegación en píldora turquesa; banda principal
en carbón; ámbar para la llamada a la acción; pie turquesa; Montserrat.

## Paleta

| Nombre          | Hex       | Uso                                                                    |
| --------------- | --------- | ---------------------------------------------------------------------- |
| Blanco          | `#FFFFFF` | Fondo base.                                                            |
| Niebla          | `#EFF6F5` | Superficie secundaria: bandas, paneles de filtros, resumen de opiniones. |
| Turquesa        | `#9DD9D2` | Marca: sección activa, pie, burbujas propias del chat, avatares.        |
| Turquesa oscuro | `#17675F` | Enlaces, foco y estados seleccionados (6,4:1 sobre blanco).            |
| Carbón          | `#353535` | Texto, botones principales y la banda de la portada.                   |
| Ámbar           | `#FFCC74` | Solo la acción principal sobre fondo carbón. Nunca adorno.             |
| Estrella        | `#E39A0B` | Estrellas de valoración.                                               |
| Verde           | `#1D6A3A` | El distintivo «100 % sin gluten» y los mensajes de éxito.              |

Los tokens viven en `frontend/src/styles.css`. Ningún componente usa colores sueltos si existe un token.

## Tipografía

Montserrat Variable (servida desde la propia app, sin Google Fonts). Titulares en 700 y en frase normal,
nunca en mayúsculas. Interfaz en 600. Texto a 16 px con interlineado 1,6.

## Composición

- Alineación a la izquierda. Ancho máximo de 1200 px; márgenes de 20 px en móvil y 40 px en escritorio.
- Un momento fuerte por pantalla. En la portada, el buscador sobre la banda carbón con el mapa real de
  todos los sitios al lado.
- Se separa con espacio, con líneas finas entre filas y con cambios de superficie (blanco y niebla).
  Nada de tarjetas con borde y sombra repetidas.
- Sin foto, la portada de un sitio es un fondo plano con el icono de su tipo: nunca una imagen inventada.
- Las fotos que no son de quien las sube (las de la demo, de Wikimedia Commons) llevan debajo su autoría
  y su licencia, en texto pequeño.
- El estado del horario va en texto, sin pastillas: «Abierto hasta las 23:30» en turquesa oscuro y
  «Cerrado, abre mañana a las 13:00» en gris. El verde queda reservado al «100 % sin gluten».
- En móvil la lista de sitios es compacta (miniatura a la izquierda) y la navegación va abajo.

## Texto

Frase normal y verbos concretos: «Añadir un sitio», «Cómo llegar», «Publicar opinión». Sin rótulos en
mayúsculas sobre los títulos, sin cadenas unidas con «·», sin «→» en botones y sin exclamaciones de relleno.
Los errores dicen qué ha pasado y qué hacer, y los estados vacíos invitan a actuar.

## Calidad mínima

Contraste AA comprobado con axe-core en las pruebas e2e (escritorio y móvil), objetivos táctiles de
44 px, foco visible, movimiento reducido respetado y revisión con capturas a 390 y 1440 px.
