from typing import Annotated

from fastapi import APIRouter, Query
from sqlalchemy import select

from app.models import Actividad, Usuario
from app.schemas import ActividadOut
from app.security import DB

router = APIRouter(prefix="/api/actividades", tags=["actividades"])


@router.get("", response_model=list[ActividadOut])
def listar(
    db: DB,
    usuario: str | None = None,
    limite: Annotated[int, Query(ge=1, le=200)] = 50,
    antes_de: Annotated[int | None, Query(description="id para paginar hacia atrás")] = None,
):
    consulta = (
        select(Actividad)
        .join(Actividad.usuario)
        .where(Usuario.archivado.is_(False))
        .order_by(Actividad.id.desc())
        .limit(limite)
    )
    if usuario:
        consulta = consulta.where(Usuario.username == usuario)
    if antes_de:
        consulta = consulta.where(Actividad.id < antes_de)
    return db.scalars(consulta).all()
