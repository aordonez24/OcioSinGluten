# Ocio Sin Gluten

Trabajo Fin de Grado de **Álvaro Ordóñez Romero**. Escuela Politécnica Superior, Universidad de Jaén.

Red social para personas celíacas: comparten restaurantes, bares y sitios de ocio con opciones sin
gluten, los valoran, opinan, suben fotos, siguen a otros miembros y charlan en un chat en tiempo real.
Funciona en la web y como **app nativa para Android e iOS** (consulta [docs/MOVIL.md](docs/MOVIL.md)).

## Funcionalidades

- **Establecimientos** en lista o en mapa, ordenables por cercanía, con búsqueda que ignora tildes y filtros por provincia,
  tipo de sitio, precio, distintivos y **abierto ahora**. Se ordenan por valoración, opiniones, visitas o nombre. Los filtros quedan
  en la URL, así que se pueden compartir.
- **Distintivos para celíacos** en cada sitio: 100 % sin gluten, carta sin gluten, cocina separada y
  acreditado por una asociación de celíacos.
- **Ficha de cada sitio**: mapa, "cómo llegar", horario, web y rango de precio, me gusta, "he estado",
  guardar en favoritos, compartir, galería de fotos, opiniones y otros sitios sin gluten cercanos.
- **Horario por días** con franjas (también de madrugada): la ficha y cada tarjeta dicen si el sitio está
  abierto ahora, hasta cuándo o cuándo abre, con la hora local del sitio (Canarias incluida).
- **Avisar de un error**: cualquier miembro puede avisar de que un sitio ha cerrado, ya no tiene opciones
  sin gluten o tiene datos mal; el aviso llega a la bandeja del equipo de administración.
- Quien añade un sitio puede **editarlo** después (borrarlo solo puede el equipo de administración).
- **Opiniones con estrellas** (1 a 5), una por persona y sitio, que se pueden editar. La valoración del
  sitio es la media de las opiniones. Se pueden marcar como útiles y ordenar por utilidad o puntuación.
- **Notificaciones** cuando alguien te sigue, opina sobre un sitio que añadiste o valora tu opinión.
- **Perfiles** con actividad, opiniones, favoritos, visitados, seguidores y seguidos; seguir a otras personas.
- **Insignias** por lo que cada persona aporta (primera opinión, añadir un sitio, haber estado en tres
  provincias, fotos, opiniones útiles…). En tu perfil ves lo que te falta para cada una; en el de los demás,
  solo las que han conseguido.
- **Comunidad** y **chat** en tiempo real (WebSockets) con historial persistente.
- **Actividades** de la comunidad agrupadas por días, filtrables por tipo y por "de quienes sigo".
- **App nativa para Android e iOS** (Capacitor) con icono y pantalla de bienvenida propios, botón «atrás»,
  compartir y vibración nativos y GPS. El APK se compila en GitHub Actions en cada push.
- **Instalable desde la web** como una app (manifiesto PWA con iconos y accesos directos) que **funciona sin
  conexión**: se abre sin cobertura y muestra lo que se consultó la última vez.
- **Gestos del móvil**: deslizar hacia abajo para recargar las listas y pasar las fotos con el dedo.
- **Recuperar la contraseña** con un enlace por email que caduca en una hora y sirve una sola vez.
  Al cambiar la contraseña se **cierran las sesiones abiertas en otros dispositivos**.
- **Vista previa al compartir**: un enlace a un sitio enviado por WhatsApp, Telegram o redes sociales
  muestra una tarjeta con la foto, el nombre, la valoración y los distintivos, dibujada con la letra y los
  colores de la app. Los buscadores
  tienen `sitemap.xml` y `robots.txt`; los perfiles no se indexan, porque dicen quién es celíaco.
- **Administración**: editar o eliminar establecimientos, suspender o eliminar cuentas y gestionar las
  consultas del formulario de contacto.
- **Privacidad**: el DNI, el email, el teléfono y la fecha de nacimiento solo los ven la propia persona
  y el equipo de administración. Cada persona puede **descargar todos sus datos** en un JSON y eliminar su
  cuenta desde Ajustes.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Python 3.11+, [FastAPI](https://fastapi.tiangolo.com), SQLAlchemy 2, Pydantic 2 |
| Base de datos | SQLite (por defecto, sin instalar nada) o PostgreSQL, con migraciones de Alembic |
| Seguridad | JWT (Bearer), bcrypt, límite de intentos de login, cabeceras de seguridad |
| Tiempo real | WebSockets nativos |
| Imágenes | Pillow: redimensionadas, convertidas a WebP y sin metadatos EXIF |
| Frontend | Vue 3 (`<script setup>`), Vite, Pinia, Vue Router, Leaflet + OpenStreetMap |
| App móvil | Capacitor 8 (Android e iOS) con el mismo frontend |
| Calidad | pytest, ruff, Playwright (e2e), axe-core (accesibilidad), GitHub Actions |

## Puesta en marcha

### Backend

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate          # Windows  (Linux/Mac: source .venv/bin/activate)
pip install -r requirements-dev.txt

python -m app.cli demo          # opcional: datos de ejemplo
uvicorn app.main:app --reload   # http://localhost:8000  ·  documentación de la API: /docs
```

### Frontend (desarrollo)

```bash
cd frontend
npm install
npm run dev                     # http://localhost:5173
```

Vite redirige `/api` y `/uploads` al backend, así que no hay que configurar URLs ni CORS.

La base de datos se crea y se actualiza sola al arrancar (migraciones de Alembic), así que no hay que
borrarla cuando cambia el modelo. Para crear una migración nueva tras tocar `app/models.py`:

```bash
alembic revision --autogenerate -m "describe el cambio"   # desde backend/; revisa el fichero generado
```

### Correo

Sin servidor SMTP (lo normal en desarrollo), los correos se guardan como ficheros `.eml` en
`backend/correos/` y se abren con cualquier programa de correo. En producción define `SMTP_HOST`,
`SMTP_USUARIO`, `SMTP_PASSWORD` y `URL_PUBLICA` (consulta `backend/.env.example`).

### Cuentas de la demo

| Email | Contraseña | Rol |
|---|---|---|
| `admin@example.com` | `Admin1234` | Administración |
| `lucia@example.com` | `Lucia1234` | Miembro |
| `marcos@example.com` | `Marcos1234` | Miembro |
| `sara@example.com` | `Sara12345` | Miembro |
| `javi@example.com` | `Javi12345` | Miembro |

Para dar permisos de administración a cualquier cuenta: `python -m app.cli admin correo@ejemplo.com`.

## Producción

### Con Docker (recomendado)

```bash
docker build -t ociosingluten .
docker run -d -p 8000:8000 -e SECRET_KEY="una-clave-larga-y-secreta" -v osg-datos:/datos ociosingluten
docker exec <contenedor> python -m app.cli demo   # opcional
```

Una sola imagen contiene la API y el frontend compilado. La base de datos y las fotos se guardan en el
volumen `/datos`.

### Sin Docker

```bash
cd frontend && npm run build
cd ../backend && uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Si existe `frontend/dist`, FastAPI también sirve el frontend, así que basta con un único proceso.

Define siempre `SECRET_KEY` (consulta `backend/.env.example`). Si no lo haces, la aplicación avisa al
arrancar.

## Tests y calidad

```bash
cd backend
pytest                 # pruebas de la API
ruff check app tests   # análisis estático
ruff format app tests  # formato
```

### Pruebas de extremo a extremo y accesibilidad

Arrancan la aplicación real con datos de demo en una base de datos temporal y la manejan con un navegador:
registro, búsqueda, alta de un sitio, opiniones, chat, moderación, navegación móvil… Además auditan las
páginas principales con [axe-core](https://github.com/dequelabs/axe-core) (WCAG 2.1 AA).

```bash
cd frontend && npm install && npm run build && cd ..
pip install -r e2e/requirements.txt
playwright install chromium        # o usa un navegador instalado: E2E_NAVEGADOR=msedge
pytest e2e
```

GitHub Actions ejecuta los tests, el linter, la compilación del frontend y las pruebas e2e en cada push.

## Estructura

```
backend/
  app/
    main.py            Crea la app, middlewares, routers y sirve el frontend
    config.py          Configuración (variables de entorno / .env)
    db.py              Conexión a la base de datos
    models.py          Tablas (SQLAlchemy)
    schemas.py         Entrada/salida de la API y validaciones (Pydantic)
    security.py        Contraseñas, tokens JWT y permisos
    cli.py             Comandos: crear admin, cargar demo, migrar
    migraciones/       Migraciones de Alembic
    demo_fotos/        Fotos de la demo (Wikimedia Commons; autoría y licencia en creditos.json)
    routers/           auth, usuarios, establecimientos, actividades, estadisticas, consultas, chat
    services/          Imágenes, correo, vista previa y tarjeta para compartir, insignias, límite de intentos…
    recursos/          Letra Montserrat (licencia OFL) y logo para dibujar las tarjetas
  tests/
frontend/
  src/
    api.js             Cliente HTTP único (token y errores)
    router.js          Rutas, títulos de página y protección por sesión o rol
    horario.js         ¿Está abierto? Horario semanal en la zona horaria del sitio
    plataforma.js      Diferencias entre la web y la app nativa (servidor, compartir, GPS, botón atrás)
    stores/            Estado global: sesión, avisos y diálogo de confirmación
    components/        Cabecera, barra móvil, tarjetas, mapa, formularios…
    views/             Páginas
  sw.js                Service worker (uso sin conexión); Vite le añade la lista de ficheros al compilar
  android/, ios/       Proyectos nativos de Capacitor (consulta docs/MOVIL.md)
  resources/           Icono y pantalla de bienvenida de la app
e2e/                   Pruebas de extremo a extremo y de accesibilidad
docs/DISENO.md         Dirección de diseño: paleta, tipografía y reglas de la interfaz
docs/MOVIL.md          App de Android e iOS: probarla, compilarla y publicarla
Dockerfile             Imagen de producción (frontend + backend)
```

## API

Documentación interactiva en `http://localhost:8000/docs`. Resumen:

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro`, `/api/auth/login` | Público |
| POST | `/api/auth/recuperar` (envía el enlace), `/api/auth/restablecer` | Público |
| GET | `/api/auth/yo` | Sesión |
| GET | `/api/estadisticas` | Público |
| GET | `/api/establecimientos`, `/api/establecimientos/{id}`, `/{id}/comentarios` | Público |
| POST | `/api/establecimientos`, `/{id}/imagenes`, `/{id}/comentarios` | Sesión |
| PUT/DELETE | `/api/establecimientos/{id}/like`, `/favorito`, `/visita` | Sesión |
| POST | `/api/establecimientos/{id}/avisos` (avisar de un error) | Sesión |
| PATCH | `/api/establecimientos/{id}` | Quien lo añadió o admin |
| DELETE | `/api/establecimientos/{id}` | Admin |
| PATCH | `/api/comentarios/{id}` (editar tu opinión) | Autor |
| PUT/DELETE | `/api/comentarios/{id}/util` | Sesión |
| GET, POST | `/api/notificaciones`, `/api/notificaciones/leidas` | Sesión |
| DELETE | `/api/establecimientos/{id}/imagenes/{img}`, `/api/comentarios/{id}` | Autor o admin |
| GET | `/api/usuarios` (comunidad) | Sesión |
| GET | `/api/usuarios/{username}` (+ `/opiniones`, `/seguidores`, `/seguidos`, `/favoritos`, `/visitados`) | Público |
| PATCH/PUT/DELETE | `/api/usuarios/yo`, `/yo/password`, `/yo/foto` | Sesión |
| GET | `/api/usuarios/yo/datos` (descargar tus datos) | Sesión |
| PUT/DELETE | `/api/usuarios/{username}/seguir` | Sesión |
| PATCH | `/api/usuarios/{username}/archivado` | Admin |
| DELETE | `/api/usuarios/{username}` | La propia cuenta o admin |
| GET | `/api/actividades?usuario=&limite=&antes_de=&siguiendo=` | Público (`siguiendo`, con sesión) |
| POST | `/api/consultas` | Público |
| GET/DELETE | `/api/consultas` | Admin |
| WS | `/api/chat?token=` | Sesión |
| GET | `/robots.txt`, `/sitemap.xml`, `/vista-previa/{id}.jpg` (tarjeta para compartir) | Público |

## Créditos de las fotos de la demo

Las fotos de los establecimientos de ejemplo son de [Wikimedia Commons](https://commons.wikimedia.org),
con licencias CC0, CC BY y CC BY-SA. La autoría y la licencia de cada una aparecen bajo la foto en la ficha
y en `backend/app/demo_fotos/creditos.json`. Los sitios de la demo son inventados: las fotos ilustran el
tipo de cocina, no son de esos locales.
