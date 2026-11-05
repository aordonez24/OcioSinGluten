from fastapi import APIRouter, HTTPException, status
from sqlalchemy import select

from app.models import Consulta
from app.schemas import ConsultaIn, ConsultaOut
from app.security import DB, Admin

router = APIRouter(prefix="/api/consultas", tags=["consultas"])


@router.post("", response_model=ConsultaOut, status_code=status.HTTP_201_CREATED)
def enviar(datos: ConsultaIn, db: DB):
    consulta = Consulta(**datos.model_dump())
    db.add(consulta)
    db.commit()
    return consulta


@router.get("", response_model=list[ConsultaOut])
def listar(db: DB, _: Admin):
    return db.scalars(select(Consulta).order_by(Consulta.creado_en.desc())).all()


@router.delete("/{consulta_id}", status_code=status.HTTP_204_NO_CONTENT)
def resolver(consulta_id: int, db: DB, _: Admin):
    consulta = db.get(Consulta, consulta_id)
    if consulta is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Consulta no encontrada")
    db.delete(consulta)
    db.commit()
