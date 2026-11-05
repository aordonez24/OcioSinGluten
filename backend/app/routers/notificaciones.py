from typing import Annotated

from fastapi import APIRouter, Query, status
from sqlalchemy import func, select, update

from app.models import Notificacion, Usuario
from app.schemas import Notificaciones
from app.security import DB, UsuarioActual

router = APIRouter(prefix="/api/notificaciones", tags=["notificaciones"])


@router.get("", response_model=Notificaciones)
def listar(db: DB, yo: UsuarioActual, limite: Annotated[int, Query(ge=1, le=100)] = 30):
    mias = (Notificacion.usuario_id == yo.id, Usuario.archivado.is_(False))
    lista = db.scalars(
        select(Notificacion)
        .join(Notificacion.actor)
        .where(*mias)
        .order_by(Notificacion.creado_en.desc(), Notificacion.id.desc())
        .limit(limite)
    ).all()
    no_leidas = db.scalar(
        select(func.count())
        .select_from(Notificacion)
        .join(Notificacion.actor)
        .where(*mias, Notificacion.leida.is_(False))
    )
    return {"no_leidas": no_leidas, "notificaciones": lista}


@router.post("/leidas", status_code=status.HTTP_204_NO_CONTENT)
def marcar_leidas(db: DB, yo: UsuarioActual):
    db.execute(update(Notificacion).where(Notificacion.usuario_id == yo.id).values(leida=True))
    db.commit()
