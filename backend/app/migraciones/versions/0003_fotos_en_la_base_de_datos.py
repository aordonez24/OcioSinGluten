"""Fotos en la base de datos

Las fotos pasan de ficheros en la carpeta de subidas a la tabla fotos, para que el servidor no
necesite disco propio. Las que ya hubiera en la carpeta se importan.

Revisión: 0003
Anterior: 0002
Fecha: 2026-11-14
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

from app.config import get_settings

revision: str = "0003"
down_revision: str | None = "0002"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    fotos = op.create_table(
        "fotos",
        sa.Column("nombre", sa.String(100), primary_key=True),
        sa.Column("datos", sa.LargeBinary(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False, server_default=sa.func.now()),
    )
    conexion = op.get_bind()
    nombres = {fila[0] for fila in conexion.execute(sa.text("SELECT fichero FROM imagenes"))}
    nombres |= {
        fila[0] for fila in conexion.execute(sa.text("SELECT foto FROM usuarios WHERE foto IS NOT NULL"))
    }
    carpeta = get_settings().uploads_dir
    filas = [
        {"nombre": nombre, "datos": (carpeta / nombre).read_bytes()}
        for nombre in sorted(nombres)
        if (carpeta / nombre).is_file()
    ]
    if filas:
        op.bulk_insert(fotos, filas)


def downgrade() -> None:
    op.drop_table("fotos")
