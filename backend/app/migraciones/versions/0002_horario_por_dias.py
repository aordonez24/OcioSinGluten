"""Horario por días y crédito de las fotos

El horario pasa de texto libre a una lista de franjas por día, para poder saber si un sitio está
abierto ahora. El texto que hubiera se conserva como nota del horario.

Revisión: 0002
Anterior: 0001
Fecha: 2026-11-06
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0002"
down_revision: str | None = "0001"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    with op.batch_alter_table("establecimientos") as tabla:
        tabla.alter_column("horario", new_column_name="nota_horario", existing_type=sa.String(300))
    with op.batch_alter_table("establecimientos") as tabla:
        tabla.add_column(sa.Column("horario", sa.JSON(), nullable=True))

    with op.batch_alter_table("imagenes") as tabla:
        tabla.add_column(sa.Column("credito", sa.String(200), nullable=True))
        tabla.add_column(sa.Column("credito_url", sa.String(300), nullable=True))


def downgrade() -> None:
    with op.batch_alter_table("imagenes") as tabla:
        tabla.drop_column("credito_url")
        tabla.drop_column("credito")

    with op.batch_alter_table("establecimientos") as tabla:
        tabla.drop_column("horario")
    with op.batch_alter_table("establecimientos") as tabla:
        tabla.alter_column("nota_horario", new_column_name="horario", existing_type=sa.String(300))
