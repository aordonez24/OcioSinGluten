# Despliegue en Render y Supabase

La aplicación se publica gratis con dos servicios:

- **Render** (plan gratuito) ejecuta la imagen del `Dockerfile`: la API y el frontend compilado en un
  solo proceso, con HTTPS y WebSockets para el chat. Su configuración está en `render.yaml`.
- **Supabase** (plan gratuito) da la base de datos PostgreSQL. Las fotos también se guardan en ella
  (tabla `fotos`), porque el disco de Render se borra cada vez que el servidor se reinicia o se duerme.

En local nada cambia: sin `DATABASE_URL` se usa SQLite.

## Límites del plan gratuito

| Qué | Consecuencia |
|---|---|
| Render duerme el servidor tras 15 minutos sin visitas | La primera visita después tarda hasta un minuto. La app instalada muestra mientras tanto lo último que se consultó. |
| Supabase pausa el proyecto tras una semana sin uso | El workflow `mantener-activo.yml` visita `/api/salud` cada tres días para evitarlo. |
| Supabase: 500 MB de base de datos | Las fotos se guardan en WebP de 1600 px como máximo (unos 100-150 KB): caben unas 3.000. |
| Un único proceso en Render | El chat y el límite de intentos viven en memoria; para varios servidores habría que llevarlos a Redis. |

## Base de datos (Supabase)

El proyecto `ociosingluten` (ref `bvtixoafufafzpgrtloo`, región Frankfurt) tiene un usuario de base de
datos `ociosingluten`, dueño de un esquema con el mismo nombre y con ese esquema como `search_path`.
Las tablas no están en `public`, así que la Data API de Supabase no las expone: solo la aplicación
puede leer los datos personales (DNI, email, teléfono). Las tablas las crean las migraciones de
Alembic al arrancar el servidor, igual que en local.

La conexión va por el *session pooler*, porque la conexión directa de Supabase solo funciona por IPv6 y
Render no la tiene:

```
DATABASE_URL=postgresql+psycopg://ociosingluten.bvtixoafufafzpgrtloo:<clave>@aws-1-eu-central-1.pooler.supabase.com:5432/postgres
```

La dirección completa, con la clave, está en `backend/.env.produccion`, que no se sube al repositorio.

## Publicar en Render

1. Abre <https://render.com/deploy?repo=https://github.com/aordonez24/OcioSinGluten> y entra con la
   cuenta de GitHub. Render lee `render.yaml` (rama `master`).
2. Pega en `DATABASE_URL` el valor de `backend/.env.produccion` y pulsa **Deploy Blueprint**.
   `SECRET_KEY` la genera Render y `URL_PUBLICA` sale sola de `RENDER_EXTERNAL_URL`.
3. La primera compilación tarda unos minutos. Cuando `/api/salud` responde, Render da el despliegue
   por bueno; con cada push a la rama se vuelve a publicar.
4. La web queda en <https://ociosingluten.onrender.com>. `mantener-activo.yml` y el APK que compila
   la CI usan esa dirección; si cambia (por ejemplo, con un dominio propio), crea en GitHub las
   variables del repositorio `URL_PUBLICA` y `SERVIDOR_APP` (Settings > Secrets and variables >
   Actions > Variables).

## Después de publicar

```bash
# Desde backend/, con DATABASE_URL apuntando a producción:
python -m app.cli admin tu-correo@ejemplo.com   # dar permisos de administración a tu cuenta
python -m app.cli demo                          # opcional: sitios y cuentas de ejemplo
```

Cuidado con `demo` en producción: crea cuentas con las contraseñas del README (incluida una de
administración) y sitios inventados. Si la cargas, cambia esas contraseñas o suspende las cuentas.

Para el correo de recuperar la contraseña hace falta un servidor SMTP (`SMTP_HOST`, `SMTP_USUARIO`,
`SMTP_PASSWORD`; por ejemplo, el de Brevo o Resend en su plan gratuito). Sin él, los correos se
guardan como ficheros en el servidor y nadie los recibe.

## Otras formas de desplegar

La imagen de Docker funciona en cualquier sitio con disco persistente sin cambiar nada: SQLite, con las
fotos dentro, se guarda en el volumen `/datos` (ver el README). El puerto se toma de la variable `PORT`.
