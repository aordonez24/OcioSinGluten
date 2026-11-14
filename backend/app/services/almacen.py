"""Dónde se guardan las fotos subidas.

- En disco (por defecto): la carpeta `uploads_dir`, que FastAPI sirve en /uploads. Es lo que se usa en
  desarrollo, en las pruebas y con Docker y un volumen.
- En Supabase Storage, si se definen SUPABASE_URL y SUPABASE_SECRET_KEY: para servidores sin disco
  persistente, como el plan gratuito de Render, donde los ficheros se borran en cada reinicio. Las
  fotos se sirven directamente desde Supabase y no despiertan al servidor.

Los nombres de los ficheros son únicos (uuid), así que una foto nunca cambia y se puede cachear siempre.
"""

import logging

import httpx

from app.config import get_settings

log = logging.getLogger("uvicorn.error")
UN_ANO = 60 * 60 * 24 * 365


def _supabase() -> tuple[str, str, str] | None:
    ajustes = get_settings()
    if ajustes.supabase_url and ajustes.supabase_secret_key:
        return ajustes.supabase_url.rstrip("/"), ajustes.supabase_secret_key, ajustes.supabase_bucket
    return None


def _cabeceras(clave: str) -> dict[str, str]:
    # Las claves nuevas (sb_secret_…) van solo en "apikey"; las antiguas (JWT), también como Bearer.
    return (
        {"apikey": clave}
        if clave.startswith("sb_")
        else {"apikey": clave, "Authorization": f"Bearer {clave}"}
    )


def en_supabase() -> bool:
    return _supabase() is not None


def url(nombre: str) -> str:
    """Dirección pública de una foto: relativa en disco, absoluta en Supabase."""
    if destino := _supabase():
        base, _, cubo = destino
        return f"{base}/storage/v1/object/public/{cubo}/{nombre}"
    return f"/uploads/{nombre}"


def guardar(nombre: str, datos: bytes, tipo: str = "image/webp") -> None:
    if destino := _supabase():
        base, clave, cubo = destino
        respuesta = httpx.post(
            f"{base}/storage/v1/object/{cubo}/{nombre}",
            content=datos,
            headers={**_cabeceras(clave), "Content-Type": tipo, "Cache-Control": f"max-age={UN_ANO}"},
            timeout=30,
        )
        respuesta.raise_for_status()
        return
    carpeta = get_settings().uploads_dir
    carpeta.mkdir(parents=True, exist_ok=True)
    (carpeta / nombre).write_bytes(datos)


def leer(nombre: str) -> bytes | None:
    if _supabase():
        try:
            respuesta = httpx.get(url(nombre), timeout=10)
            respuesta.raise_for_status()
        except httpx.HTTPError as error:
            log.warning("No se pudo leer la foto %s de Supabase: %s", nombre, error)
            return None
        return respuesta.content
    ruta = get_settings().uploads_dir / nombre
    return ruta.read_bytes() if ruta.is_file() else None


def borrar(*nombres: str | None) -> None:
    nombres = [n for n in nombres if n]
    if not nombres:
        return
    if destino := _supabase():
        base, clave, cubo = destino
        try:
            httpx.request(
                "DELETE",
                f"{base}/storage/v1/object/{cubo}",
                json={"prefixes": nombres},
                headers=_cabeceras(clave),
                timeout=15,
            ).raise_for_status()
        except httpx.HTTPError as error:
            # Lo importante (la fila en la base de datos) ya se ha borrado; la foto queda huérfana.
            log.warning("No se pudieron borrar %s de Supabase: %s", nombres, error)
        return
    carpeta = get_settings().uploads_dir
    for nombre in nombres:
        (carpeta / nombre).unlink(missing_ok=True)
