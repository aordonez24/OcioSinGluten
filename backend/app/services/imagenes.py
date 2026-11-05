"""Las imágenes se guardan como ficheros (no como BLOB en la base de datos) y se sirven
como estáticos. Se redimensionan, se convierten a WebP y se les quitan los metadatos EXIF
(que pueden incluir la ubicación GPS de quien hizo la foto)."""

import uuid
from io import BytesIO

from fastapi import HTTPException, UploadFile, status
from PIL import Image, ImageOps, UnidentifiedImageError

from app.config import get_settings


def guardar_imagen(archivo: UploadFile, max_lado: int | None = None) -> str:
    ajustes = get_settings()
    limite = ajustes.max_imagen_mb * 1024 * 1024
    datos = archivo.file.read(limite + 1)
    if len(datos) > limite:
        raise HTTPException(
            status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
            f"La imagen no puede superar {ajustes.max_imagen_mb} MB",
        )

    try:
        imagen = Image.open(BytesIO(datos))
        imagen = ImageOps.exif_transpose(imagen)
        imagen.thumbnail((max_lado or ajustes.max_lado_imagen_px,) * 2)
        if imagen.mode not in ("RGB", "RGBA"):
            imagen = imagen.convert("RGB")
    except (UnidentifiedImageError, OSError, Image.DecompressionBombError):
        raise HTTPException(
            status.HTTP_422_UNPROCESSABLE_CONTENT, "El archivo no es una imagen válida"
        ) from None

    ajustes.uploads_dir.mkdir(parents=True, exist_ok=True)
    nombre = f"{uuid.uuid4().hex}.webp"
    imagen.save(ajustes.uploads_dir / nombre, "WEBP", quality=82)
    return nombre


def borrar_imagenes(*ficheros: str | None) -> None:
    carpeta = get_settings().uploads_dir
    for fichero in ficheros:
        if fichero:
            (carpeta / fichero).unlink(missing_ok=True)
