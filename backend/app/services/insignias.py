"""Insignias del perfil: reconocen lo que cada persona aporta a la comunidad.

Se calculan al vuelo a partir de lo que ya hay en la base de datos (opiniones, visitas, fotos…),
así que no hay nada que guardar ni que se pueda desincronizar.
"""

from dataclasses import dataclass

from sqlalchemy import distinct, func, select
from sqlalchemy.orm import Session

from app.models import Comentario, Establecimiento, Imagen, Usuario, seguimientos, utiles, visitas


@dataclass(frozen=True)
class Definicion:
    clave: str
    titulo: str
    descripcion: str  # qué hay que hacer para conseguirla
    medida: str  # qué cifra cuenta
    meta: int


INSIGNIAS = (
    Definicion("primera_opinion", "Primera opinión", "Publicar una opinión sobre un sitio", "opiniones", 1),
    Definicion("abre_camino", "Abre camino", "Añadir un sitio que aún no estaba", "anadidos", 1),
    Definicion("trotamundos", "Trotamundos", "Haber estado en sitios de 3 provincias", "provincias", 3),
    Definicion("buen_ojo", "Buen ojo", "Subir 5 fotos de los sitios", "fotos", 5),
    Definicion("ayuda", "Opiniones que ayudan", "Que tus opiniones resulten útiles 5 veces", "utiles", 5),
    Definicion("voz", "Voz de la comunidad", "Publicar 10 opiniones", "opiniones", 10),
    Definicion("compania", "En buena compañía", "Tener 10 personas que te siguen", "seguidores", 10),
)


def _cifras(db: Session, usuario: Usuario) -> dict[str, int]:
    def contar(consulta) -> int:
        return db.scalar(consulta) or 0

    return {
        "opiniones": contar(select(func.count()).where(Comentario.autor_id == usuario.id)),
        "anadidos": contar(select(func.count()).where(Establecimiento.creado_por_id == usuario.id)),
        "fotos": contar(select(func.count()).where(Imagen.autor_id == usuario.id)),
        "provincias": contar(
            select(func.count(distinct(Establecimiento.provincia)))
            .join(visitas, visitas.c.establecimiento_id == Establecimiento.id)
            .where(visitas.c.usuario_id == usuario.id)
        ),
        "utiles": contar(
            select(func.count())
            .select_from(utiles)
            .join(Comentario, Comentario.id == utiles.c.comentario_id)
            .where(Comentario.autor_id == usuario.id)
        ),
        "seguidores": contar(select(func.count()).where(seguimientos.c.seguido_id == usuario.id)),
    }


def calcular(db: Session, usuario: Usuario) -> list[dict]:
    """Todas las insignias con su progreso; primero las conseguidas."""
    cifras = _cifras(db, usuario)
    lista = [
        {
            "clave": d.clave,
            "titulo": d.titulo,
            "descripcion": d.descripcion,
            "actual": min(cifras[d.medida], d.meta),
            "meta": d.meta,
            "conseguida": cifras[d.medida] >= d.meta,
        }
        for d in INSIGNIAS
    ]
    return sorted(lista, key=lambda i: not i["conseguida"])  # sorted es estable: mantiene el orden
