from fastapi import APIRouter
from sqlalchemy import func, select

from app.models import Comentario, Establecimiento, Usuario
from app.schemas import Estadisticas
from app.security import DB

router = APIRouter(prefix="/api/estadisticas", tags=["estadisticas"])


@router.get("", response_model=Estadisticas)
def estadisticas(db: DB):
    return Estadisticas(
        establecimientos=db.scalar(select(func.count(Establecimiento.id))),
        miembros=db.scalar(select(func.count(Usuario.id)).where(Usuario.archivado.is_(False))),
        opiniones=db.scalar(select(func.count(Comentario.id))),
        provincias=db.scalar(select(func.count(func.distinct(Establecimiento.provincia)))),
    )
