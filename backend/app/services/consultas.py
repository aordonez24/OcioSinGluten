"""Consultas de base de datos reutilizadas por varios routers."""

from fastapi import HTTPException, status
from sqlalchemy import delete, exists, select
from sqlalchemy.orm import Session

from app.models import Actividad, Establecimiento, TipoActividad, Usuario


def usuario_por_username(db: Session, username: str, *, incluir_archivados: bool = False) -> Usuario:
    usuario = db.scalar(select(Usuario).where(Usuario.username == username))
    if usuario is None or (usuario.archivado and not incluir_archivados):
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Usuario no encontrado")
    return usuario


def establecimiento_o_404(db: Session, est_id: int) -> Establecimiento:
    est = db.get(Establecimiento, est_id)
    if est is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Establecimiento no encontrado")
    return est


def existe_fila(db: Session, tabla, **filtros) -> bool:
    condiciones = [tabla.c[col] == valor for col, valor in filtros.items()]
    return bool(db.scalar(select(exists().where(*condiciones))))


def registrar_actividad(db: Session, usuario_id: int, est_id: int, tipo: TipoActividad) -> None:
    db.add(Actividad(usuario_id=usuario_id, establecimiento_id=est_id, tipo=tipo))


def borrar_actividad(db: Session, usuario_id: int, est_id: int, tipo: TipoActividad) -> None:
    db.execute(
        delete(Actividad).where(
            Actividad.usuario_id == usuario_id,
            Actividad.establecimiento_id == est_id,
            Actividad.tipo == tipo,
        )
    )
