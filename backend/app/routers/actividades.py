from typing import Annotated

from fastapi import APIRouter, HTTPException, Query, status
from sqlalchemy import select

from app.models import Actividad, Usuario, seguimientos
from app.schemas import ActividadOut
from app.security import DB, UsuarioOpcional

router = APIRouter(prefix="/api/actividades", tags=["actividades"])


@router.get("", response_model=list[ActividadOut])
def listar(
    db: DB,
    yo: UsuarioOpcional,
    usuario: str | None = None,
    siguiendo: Annotated[bool, Query(description="Solo de las personas a las que sigo")] = False,
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
    if siguiendo:
        if yo is None:
            raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Inicia sesión para ver a quién sigues")
        sigo = select(seguimientos.c.seguido_id).where(seguimientos.c.seguidor_id == yo.id)
        consulta = consulta.where(Actividad.usuario_id.in_(sigo))
    if antes_de:
        consulta = consulta.where(Actividad.id < antes_de)
    return db.scalars(consulta).all()
