"""Modelo de datos.

Las relaciones N:M (seguir, favoritos, visitas, me gusta) son tablas simples con clave
primaria compuesta: así es imposible duplicar un like o una visita, y al borrar un
usuario o un establecimiento la base de datos limpia todo en cascada.
"""

import enum
from datetime import UTC, date, datetime

from sqlalchemy import (
    Column,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    String,
    Table,
    Text,
    UniqueConstraint,
    func,
    select,
)
from sqlalchemy.orm import Mapped, column_property, mapped_column, relationship

from app.db import Base


def ahora() -> datetime:
    return datetime.now(UTC)


class Rol(enum.StrEnum):
    COMUN = "comun"
    ADMIN = "admin"


class TipoActividad(enum.StrEnum):
    PUBLICACION = "publicacion"
    COMENTARIO = "comentario"
    VISITA = "visita"
    LIKE = "like"


def _tabla_usuario_establecimiento(nombre: str) -> Table:
    return Table(
        nombre,
        Base.metadata,
        Column("usuario_id", ForeignKey("usuarios.id", ondelete="CASCADE"), primary_key=True),
        Column(
            "establecimiento_id",
            ForeignKey("establecimientos.id", ondelete="CASCADE"),
            primary_key=True,
            index=True,
        ),
        Column("creado_en", DateTime(timezone=True), default=ahora, nullable=False),
    )


seguimientos = Table(
    "seguimientos",
    Base.metadata,
    Column("seguidor_id", ForeignKey("usuarios.id", ondelete="CASCADE"), primary_key=True),
    Column("seguido_id", ForeignKey("usuarios.id", ondelete="CASCADE"), primary_key=True, index=True),
    Column("creado_en", DateTime(timezone=True), default=ahora, nullable=False),
)
favoritos = _tabla_usuario_establecimiento("favoritos")
visitas = _tabla_usuario_establecimiento("visitas")
likes = _tabla_usuario_establecimiento("likes")


class Usuario(Base):
    __tablename__ = "usuarios"

    id: Mapped[int] = mapped_column(primary_key=True)
    username: Mapped[str] = mapped_column(String(30), unique=True, index=True)
    email: Mapped[str] = mapped_column(String(254), unique=True, index=True)
    dni: Mapped[str] = mapped_column(String(9), unique=True)
    password_hash: Mapped[str] = mapped_column(String(100))
    nombre: Mapped[str] = mapped_column(String(50))
    apellidos: Mapped[str] = mapped_column(String(80))
    fecha_nacimiento: Mapped[date]
    telefono: Mapped[str] = mapped_column(String(15))
    foto: Mapped[str | None] = mapped_column(String(100))
    rol: Mapped[Rol] = mapped_column(Enum(Rol, native_enum=False), default=Rol.COMUN)
    archivado: Mapped[bool] = mapped_column(default=False)
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora)

    # Diferidos: solo se calculan cuando se piden (perfil y listado de la comunidad).
    num_seguidores: Mapped[int] = column_property(
        select(func.count())
        .where(seguimientos.c.seguido_id == id)
        .correlate_except(seguimientos)
        .scalar_subquery(),
        deferred=True,
    )
    num_seguidos: Mapped[int] = column_property(
        select(func.count())
        .where(seguimientos.c.seguidor_id == id)
        .correlate_except(seguimientos)
        .scalar_subquery(),
        deferred=True,
    )

    @property
    def es_admin(self) -> bool:
        return self.rol == Rol.ADMIN


class Imagen(Base):
    __tablename__ = "imagenes"

    id: Mapped[int] = mapped_column(primary_key=True)
    establecimiento_id: Mapped[int] = mapped_column(
        ForeignKey("establecimientos.id", ondelete="CASCADE"), index=True
    )
    autor_id: Mapped[int | None] = mapped_column(ForeignKey("usuarios.id", ondelete="SET NULL"))
    fichero: Mapped[str] = mapped_column(String(100))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora)


class Establecimiento(Base):
    __tablename__ = "establecimientos"
    __table_args__ = (UniqueConstraint("nombre", "cod_postal"),)

    id: Mapped[int] = mapped_column(primary_key=True)
    nombre: Mapped[str] = mapped_column(String(60), index=True)
    telefono: Mapped[str] = mapped_column(String(15))
    calle: Mapped[str] = mapped_column(String(120))
    cod_postal: Mapped[str] = mapped_column(String(5))
    localidad: Mapped[str] = mapped_column(String(60))
    provincia: Mapped[str] = mapped_column(String(60))
    pais: Mapped[str] = mapped_column(String(60), default="España")
    latitud: Mapped[float | None] = mapped_column(Float)
    longitud: Mapped[float | None] = mapped_column(Float)
    creado_por_id: Mapped[int | None] = mapped_column(ForeignKey("usuarios.id", ondelete="SET NULL"))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora)

    imagenes: Mapped[list[Imagen]] = relationship(order_by=Imagen.id, viewonly=True)
    creado_por: Mapped[Usuario | None] = relationship(viewonly=True)

    # Contadores calculados por la propia base de datos en la misma consulta (sin N+1).
    num_likes: Mapped[int] = column_property(
        select(func.count()).where(likes.c.establecimiento_id == id).correlate_except(likes).scalar_subquery()
    )
    num_visitas: Mapped[int] = column_property(
        select(func.count())
        .where(visitas.c.establecimiento_id == id)
        .correlate_except(visitas)
        .scalar_subquery()
    )
    portada: Mapped[str | None] = column_property(
        select(Imagen.fichero)
        .where(Imagen.establecimiento_id == id)
        .order_by(Imagen.id)
        .limit(1)
        .correlate_except(Imagen)
        .scalar_subquery()
    )

    @property
    def valoracion(self) -> float | None:
        """Estrellas (0-5): proporción de visitantes que han dado like."""
        if not self.num_visitas:
            return None
        return round(min(self.num_likes / self.num_visitas, 1) * 5, 1)


class Comentario(Base):
    __tablename__ = "comentarios"

    id: Mapped[int] = mapped_column(primary_key=True)
    establecimiento_id: Mapped[int] = mapped_column(
        ForeignKey("establecimientos.id", ondelete="CASCADE"), index=True
    )
    autor_id: Mapped[int] = mapped_column(ForeignKey("usuarios.id", ondelete="CASCADE"))
    mensaje: Mapped[str] = mapped_column(String(280))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora)

    autor: Mapped[Usuario] = relationship(lazy="joined")


class Actividad(Base):
    __tablename__ = "actividades"

    id: Mapped[int] = mapped_column(primary_key=True)
    usuario_id: Mapped[int] = mapped_column(ForeignKey("usuarios.id", ondelete="CASCADE"), index=True)
    establecimiento_id: Mapped[int] = mapped_column(ForeignKey("establecimientos.id", ondelete="CASCADE"))
    tipo: Mapped[TipoActividad] = mapped_column(Enum(TipoActividad, native_enum=False))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora, index=True)

    usuario: Mapped[Usuario] = relationship(lazy="joined")
    establecimiento: Mapped[Establecimiento] = relationship(lazy="joined")


class Consulta(Base):
    __tablename__ = "consultas"

    id: Mapped[int] = mapped_column(primary_key=True)
    nombre: Mapped[str] = mapped_column(String(60))
    email: Mapped[str] = mapped_column(String(254))
    mensaje: Mapped[str] = mapped_column(Text)
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora)


class Mensaje(Base):
    """Mensajes del chat de la comunidad (se guardan para mostrar el historial)."""

    __tablename__ = "mensajes"

    id: Mapped[int] = mapped_column(primary_key=True)
    autor_id: Mapped[int] = mapped_column(ForeignKey("usuarios.id", ondelete="CASCADE"))
    texto: Mapped[str] = mapped_column(String(500))
    creado_en: Mapped[datetime] = mapped_column(DateTime(timezone=True), default=ahora, index=True)

    autor: Mapped[Usuario] = relationship(lazy="joined")
