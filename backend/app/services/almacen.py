"""Guarda, lee y borra el contenido de las fotos subidas, que vive en la tabla fotos.

Los nombres son únicos (uuid), así que una foto nunca cambia y el navegador la puede cachear siempre.
Las funciones no hacen commit: la foto se guarda o se borra en la misma transacción que la fila que
la usa (la imagen de un sitio, la foto de perfil), así nunca quedan fotos huérfanas ni enlaces rotos.
"""

from sqlalchemy import delete, select
from sqlalchemy.orm import Session

from app.models import Foto


def guardar(db: Session, nombre: str, datos: bytes) -> None:
    db.add(Foto(nombre=nombre, datos=datos))


def leer(db: Session, nombre: str) -> bytes | None:
    return db.scalar(select(Foto.datos).where(Foto.nombre == nombre))


def borrar(db: Session, *nombres: str | None) -> None:
    if nombres := [n for n in nombres if n]:
        db.execute(delete(Foto).where(Foto.nombre.in_(nombres)))
