"""Sitios importados de OpenStreetMap

El teléfono de un establecimiento pasa a ser opcional (muchos sitios reales no lo publican) y se
guarda el identificador de OpenStreetMap de los sitios importados, para no duplicarlos al volver a
importar y para enlazar a su ficha (lo pide la licencia ODbL).

Revisión: 0005
Anterior: 0004
Fecha: 2026-11-14
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0005"
down_revision: str | None = "0004"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    with op.batch_alter_table("establecimientos") as tabla:
        tabla.alter_column("telefono", existing_type=sa.String(15), nullable=True)
        tabla.add_column(sa.Column("osm_id", sa.String(30), nullable=True))
        tabla.create_unique_constraint("uq_establecimientos_osm_id", ["osm_id"])


def downgrade() -> None:
    with op.batch_alter_table("establecimientos") as tabla:
        tabla.drop_constraint("uq_establecimientos_osm_id", type_="unique")
        tabla.drop_column("osm_id")
