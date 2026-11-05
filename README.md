# Ocio Sin Gluten

Trabajo Fin de Grado de **Álvaro Ordóñez Romero**. Escuela Politécnica Superior, Universidad de Jaén.

Red social para personas celíacas: comparten restaurantes, bares y sitios de ocio con opciones sin
gluten, los valoran, opinan, suben fotos, siguen a otros miembros y charlan en un chat en tiempo real.

## Funcionalidades

- **Explorar** establecimientos en lista o en mapa, con búsqueda que ignora tildes y filtros por provincia,
  tipo de sitio y distintivos. Se ordenan por valoración, opiniones, visitas o nombre. Los filtros quedan
  en la URL, así que se pueden compartir.
- **Distintivos para celíacos** en cada sitio: 100 % sin gluten, carta sin gluten, cocina separada y
  acreditado por una asociación de celíacos.
- **Ficha de cada sitio**: mapa, "cómo llegar", me gusta, "he estado", guardar en favoritos, compartir,
  galería de fotos y opiniones.
- **Opiniones con estrellas** (1 a 5), una por persona y sitio, que se pueden editar. La valoración del
  sitio es la media de las opiniones.
- **Perfiles** con actividad, favoritos, visitados, seguidores y seguidos; seguir a otras personas.
- **Comunidad** y **chat** en tiempo real (WebSockets) con historial persistente.
- **Actividad** de la comunidad agrupada por días y filtrable por tipo.
- **Administración**: editar o eliminar establecimientos, suspender o eliminar cuentas y gestionar las
  consultas del formulario de contacto.
- **Privacidad**: el DNI, el email, el teléfono y la fecha de nacimiento solo los ven la propia persona
  y el equipo de administración.

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Python 3.11+, [FastAPI](https://fastapi.tiangolo.com), SQLAlchemy 2, Pydantic 2 |
| Base de datos | SQLite (por defecto, sin instalar nada) o PostgreSQL |
| Seguridad | JWT (Bearer), bcrypt, límite de intentos de login, cabeceras de seguridad |
| Tiempo real | WebSockets nativos |
| Imágenes | Pillow: redimensionadas, convertidas a WebP y sin metadatos EXIF |
| Frontend | Vue 3 (`<script setup>`), Vite, Pinia, Vue Router, Leaflet + OpenStreetMap |
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
    cli.py             Comandos: crear admin, cargar demo
    routers/           auth, usuarios, establecimientos, actividades, estadisticas, consultas, chat
    services/          Imágenes, geolocalización, límite de intentos y consultas reutilizables
  tests/
frontend/
  src/
    api.js             Cliente HTTP único (token y errores)
    router.js          Rutas, títulos de página y protección por sesión o rol
    stores/            Estado global: sesión, avisos y diálogo de confirmación
    components/        Cabecera, barra móvil, tarjetas, mapa, formularios…
    views/             Páginas
e2e/                   Pruebas de extremo a extremo y de accesibilidad
Dockerfile             Imagen de producción (frontend + backend)
```

## API

Documentación interactiva en `http://localhost:8000/docs`. Resumen:

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/auth/registro`, `/api/auth/login` | Público |
| GET | `/api/auth/yo` | Sesión |
| GET | `/api/estadisticas` | Público |
| GET | `/api/establecimientos`, `/api/establecimientos/{id}`, `/{id}/comentarios` | Público |
| POST | `/api/establecimientos`, `/{id}/imagenes`, `/{id}/comentarios` | Sesión |
| PUT/DELETE | `/api/establecimientos/{id}/like` · `/favorito` · `/visita` | Sesión |
| PATCH/DELETE | `/api/establecimientos/{id}` | Admin |
| PATCH | `/api/comentarios/{id}` (editar tu opinión) | Autor |
| DELETE | `/api/establecimientos/{id}/imagenes/{img}`, `/api/comentarios/{id}` | Autor o admin |
| GET | `/api/usuarios` (comunidad) | Sesión |
| GET | `/api/usuarios/{username}` (+ `/seguidores`, `/seguidos`, `/favoritos`, `/visitados`) | Público |
| PATCH/PUT/DELETE | `/api/usuarios/yo`, `/yo/password`, `/yo/foto` | Sesión |
| PUT/DELETE | `/api/usuarios/{username}/seguir` | Sesión |
| PATCH | `/api/usuarios/{username}/archivado` | Admin |
| DELETE | `/api/usuarios/{username}` | La propia cuenta o admin |
| GET | `/api/actividades?usuario=&limite=&antes_de=` | Público |
| POST | `/api/consultas` | Público |
| GET/DELETE | `/api/consultas` | Admin |
| WS | `/api/chat?token=` | Sesión |
