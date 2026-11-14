# Despliegue en Render y Supabase

La aplicación se publica gratis con dos servicios:

- **Render** (plan gratuito) ejecuta la imagen del `Dockerfile`: la API y el frontend compilado en un
  solo proceso, con HTTPS y WebSockets para el chat. Su configuración está en `render.yaml`.
- **Supabase** (plan gratuito) guarda los datos en PostgreSQL y las fotos en Supabase Storage, porque
  el disco de Render se borra cada vez que el servidor se reinicia o se duerme.

En local nada cambia: sin las variables de Supabase se usa SQLite y la carpeta `backend/uploads/`.

## Límites del plan gratuito

| Qué | Consecuencia |
|---|---|
| Render duerme el servidor tras 15 minutos sin visitas | La primera visita después tarda hasta un minuto. La app instalada muestra mientras tanto lo último que se consultó. |
| Supabase pausa el proyecto tras una semana sin uso | El workflow `mantener-activo.yml` visita `/api/salud` cada tres días para evitarlo. |
| Supabase: 500 MB de base de datos y 1 GB de fotos | Las fotos se guardan en WebP de 1600 px como máximo (unos 150 KB): caben miles. |
| Un único proceso en Render | El chat y el límite de intentos viven en memoria; para varios servidores habría que llevarlos a Redis. |

## Base de datos (Supabase)

El proyecto `ociosingluten` (región Frankfurt) tiene:

- Un usuario de base de datos `ociosingluten`, dueño de un esquema con el mismo nombre y con ese
  esquema como `search_path`. Las tablas no están en `public`, así que la Data API de Supabase no las
  expone: solo la aplicación puede leer los datos personales (DNI, email, teléfono).
- Un cubo público `fotos` en Storage, que solo admite WebP de hasta 5 MB. Cualquiera puede ver las
  fotos; solo el servidor, con la clave secreta, puede subirlas o borrarlas.

Las tablas las crean las migraciones de Alembic al arrancar el servidor, igual que en local.

La conexión se hace por el *session pooler* (`aws-1-eu-central-1.pooler.supabase.com:5432`), porque la
conexión directa de Supabase solo funciona por IPv6 y Render no la tiene:

```
DATABASE_URL=postgresql+psycopg://ociosingluten.<ref-del-proyecto>:<clave>@aws-1-eu-central-1.pooler.supabase.com:5432/postgres
```

## Publicar en Render

1. En [render.com](https://render.com), entra con la cuenta de GitHub.
2. **New > Blueprint**, elige el repositorio `OcioSinGluten` y la rama que quieras publicar. Render
   lee `render.yaml` y pide las variables marcadas como secretas:

   | Variable | Valor |
   |---|---|
   | `DATABASE_URL` | La de arriba, con la clave del usuario `ociosingluten` |
   | `SUPABASE_URL` | `https://<ref-del-proyecto>.supabase.co` |
   | `SUPABASE_SECRET_KEY` | Supabase > Project Settings > API Keys > Secret keys (`sb_secret_…`) |
   | `URL_PUBLICA` | La dirección que te dé Render, por ejemplo `https://ociosingluten.onrender.com` |

   `SECRET_KEY` la genera Render sola.
3. **Apply**. La primera compilación tarda unos minutos. Cuando `/api/salud` responde, Render da el
   despliegue por bueno; con cada push a esa rama se vuelve a publicar.
4. En GitHub, crea la variable del repositorio `URL_PUBLICA` con la misma dirección (Settings >
   Secrets and variables > Actions > Variables) para que funcione `mantener-activo.yml`. Si además
   creas `SERVIDOR_APP` con esa dirección, el APK que compila la CI se conecta al servidor publicado.

## Después de publicar

```bash
# Desde backend/, con DATABASE_URL (y SUPABASE_*, para las fotos) apuntando a producción:
python -m app.cli admin tu-correo@ejemplo.com   # dar permisos de administración a tu cuenta
python -m app.cli demo                          # opcional: sitios y cuentas de ejemplo
```

Cuidado con `demo` en producción: crea cuentas con las contraseñas del README (incluida una de
administración) y sitios inventados. Si la cargas, cambia esas contraseñas o suspende las cuentas.

Para el correo de recuperar la contraseña hace falta un servidor SMTP (`SMTP_HOST`, `SMTP_USUARIO`,
`SMTP_PASSWORD`; por ejemplo, el de Brevo o Resend en su plan gratuito). Sin él, los correos se
guardan como ficheros en el servidor y nadie los recibe.

## Otras formas de desplegar

La imagen de Docker funciona en cualquier sitio con disco persistente sin cambiar nada: SQLite y las
fotos se guardan en el volumen `/datos` (ver el README). El puerto se toma de la variable `PORT`.
