"""Menos datos personales en las cuentas

Se dejan de pedir el DNI, el teléfono y la fecha de nacimiento: la aplicación no los usa para nada y,
según el RGPD, no se deben recoger datos que no hacen falta. Los apellidos pasan a ser opcionales.

Revisión: 0004
Anterior: 0003
Fecha: 2026-11-14
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0004"
down_revision: str | None = "0003"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    with op.batch_alter_table("usuarios") as tabla:
        tabla.drop_column("dni")  # su restricción única se va con la columna
        tabla.drop_column("fecha_nacimiento")
        tabla.drop_column("telefono")
        tabla.alter_column("apellidos", existing_type=sa.String(80), nullable=True)


def downgrade() -> None:
    # Los datos borrados no se pueden recuperar: las columnas vuelven vacías.
    with op.batch_alter_table("usuarios") as tabla:
        tabla.add_column(sa.Column("dni", sa.String(9), nullable=True))
        tabla.add_column(sa.Column("fecha_nacimiento", sa.Date(), nullable=True))
        tabla.add_column(sa.Column("telefono", sa.String(15), nullable=True))
