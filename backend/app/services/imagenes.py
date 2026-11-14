"""Las fotos se redimensionan, se convierten a WebP y se les quitan los metadatos EXIF (que pueden
incluir la ubicación GPS de quien hizo la foto). Se guardan en la base de datos (ver almacen.py)."""

import uuid
from io import BytesIO

from fastapi import HTTPException, UploadFile, status
from PIL import Image, ImageOps, UnidentifiedImageError
from sqlalchemy.orm import Session

from app.config import get_settings
from app.services import almacen


def guardar_imagen(db: Session, archivo: UploadFile, max_lado: int | None = None) -> str:
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

    salida = BytesIO()
    imagen.save(salida, "WEBP", quality=82)
    nombre = f"{uuid.uuid4().hex}.webp"
    almacen.guardar(db, nombre, salida.getvalue())
    return nombre


borrar_imagenes = almacen.borrar
