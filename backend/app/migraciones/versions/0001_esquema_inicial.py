"""esquema inicial

Revisión: 0001
Anterior: ninguna
Fecha: 2026-11-06 11:50:22.295553
"""

from collections.abc import Sequence

import sqlalchemy as sa
from alembic import op

revision: str = "0001"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    op.create_table(
        "usuarios",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("username", sa.String(length=30), nullable=False),
        sa.Column("email", sa.String(length=254), nullable=False),
        sa.Column("dni", sa.String(length=9), nullable=False),
        sa.Column("password_hash", sa.String(length=100), nullable=False),
        sa.Column("nombre", sa.String(length=50), nullable=False),
        sa.Column("apellidos", sa.String(length=80), nullable=False),
        sa.Column("fecha_nacimiento", sa.Date(), nullable=False),
        sa.Column("telefono", sa.String(length=15), nullable=False),
        sa.Column("foto", sa.String(length=100), nullable=True),
        sa.Column("rol", sa.Enum("COMUN", "ADMIN", name="rol", native_enum=False), nullable=False),
        sa.Column("archivado", sa.Boolean(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("dni"),
    )
    with op.batch_alter_table("usuarios", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_usuarios_email"), ["email"], unique=True)
        batch_op.create_index(batch_op.f("ix_usuarios_username"), ["username"], unique=True)

    op.create_table(
        "establecimientos",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("nombre", sa.String(length=60), nullable=False),
        sa.Column("telefono", sa.String(length=15), nullable=False),
        sa.Column("calle", sa.String(length=120), nullable=False),
        sa.Column("cod_postal", sa.String(length=5), nullable=False),
        sa.Column("localidad", sa.String(length=60), nullable=False),
        sa.Column("provincia", sa.String(length=60), nullable=False),
        sa.Column("pais", sa.String(length=60), nullable=False),
        sa.Column(
            "categoria",
            sa.Enum(
                "RESTAURANTE",
                "CAFETERIA",
                "PANADERIA",
                "BAR",
                "HELADERIA",
                "OTRO",
                name="categoria",
                native_enum=False,
            ),
            nullable=False,
        ),
        sa.Column("todo_sin_gluten", sa.Boolean(), nullable=False),
        sa.Column("carta_sin_gluten", sa.Boolean(), nullable=False),
        sa.Column("cocina_separada", sa.Boolean(), nullable=False),
        sa.Column("acreditado", sa.Boolean(), nullable=False),
        sa.Column("precio", sa.SmallInteger(), nullable=True),
        sa.Column("horario", sa.String(length=300), nullable=True),
        sa.Column("web", sa.String(length=200), nullable=True),
        sa.Column("latitud", sa.Float(), nullable=True),
        sa.Column("longitud", sa.Float(), nullable=True),
        sa.Column("creado_por_id", sa.Integer(), nullable=True),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["creado_por_id"], ["usuarios.id"], ondelete="SET NULL"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("nombre", "cod_postal"),
    )
    with op.batch_alter_table("establecimientos", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_establecimientos_nombre"), ["nombre"], unique=False)

    op.create_table(
        "mensajes",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("autor_id", sa.Integer(), nullable=False),
        sa.Column("texto", sa.String(length=500), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["autor_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    with op.batch_alter_table("mensajes", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_mensajes_creado_en"), ["creado_en"], unique=False)

    op.create_table(
        "seguimientos",
        sa.Column("seguidor_id", sa.Integer(), nullable=False),
        sa.Column("seguido_id", sa.Integer(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["seguido_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["seguidor_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("seguidor_id", "seguido_id"),
    )
    with op.batch_alter_table("seguimientos", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_seguimientos_seguido_id"), ["seguido_id"], unique=False)

    op.create_table(
        "actividades",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column(
            "tipo",
            sa.Enum("PUBLICACION", "COMENTARIO", "VISITA", "LIKE", name="tipoactividad", native_enum=False),
            nullable=False,
        ),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    with op.batch_alter_table("actividades", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_actividades_creado_en"), ["creado_en"], unique=False)
        batch_op.create_index(batch_op.f("ix_actividades_usuario_id"), ["usuario_id"], unique=False)

    op.create_table(
        "comentarios",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column("autor_id", sa.Integer(), nullable=False),
        sa.Column("puntuacion", sa.SmallInteger(), nullable=False),
        sa.Column("mensaje", sa.String(length=500), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.Column("editado_en", sa.DateTime(timezone=True), nullable=True),
        sa.ForeignKeyConstraint(["autor_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("establecimiento_id", "autor_id"),
    )
    with op.batch_alter_table("comentarios", schema=None) as batch_op:
        batch_op.create_index(
            batch_op.f("ix_comentarios_establecimiento_id"), ["establecimiento_id"], unique=False
        )

    op.create_table(
        "consultas",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("nombre", sa.String(length=60), nullable=False),
        sa.Column("email", sa.String(length=254), nullable=False),
        sa.Column("mensaje", sa.Text(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=True),
        sa.Column(
            "motivo",
            sa.Enum(
                "CERRADO", "SIN_OPCIONES", "DATOS_INCORRECTOS", "OTRO", name="motivoaviso", native_enum=False
            ),
            nullable=True,
        ),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_table(
        "favoritos",
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("usuario_id", "establecimiento_id"),
    )
    with op.batch_alter_table("favoritos", schema=None) as batch_op:
        batch_op.create_index(
            batch_op.f("ix_favoritos_establecimiento_id"), ["establecimiento_id"], unique=False
        )

    op.create_table(
        "imagenes",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column("autor_id", sa.Integer(), nullable=True),
        sa.Column("fichero", sa.String(length=100), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["autor_id"], ["usuarios.id"], ondelete="SET NULL"),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    with op.batch_alter_table("imagenes", schema=None) as batch_op:
        batch_op.create_index(
            batch_op.f("ix_imagenes_establecimiento_id"), ["establecimiento_id"], unique=False
        )

    op.create_table(
        "likes",
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("usuario_id", "establecimiento_id"),
    )
    with op.batch_alter_table("likes", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_likes_establecimiento_id"), ["establecimiento_id"], unique=False)

    op.create_table(
        "notificaciones",
        sa.Column("id", sa.Integer(), nullable=False),
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("actor_id", sa.Integer(), nullable=False),
        sa.Column(
            "tipo",
            sa.Enum("SEGUIDOR", "OPINION", "UTIL", name="tiponotificacion", native_enum=False),
            nullable=False,
        ),
        sa.Column("establecimiento_id", sa.Integer(), nullable=True),
        sa.Column("leida", sa.Boolean(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["actor_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
    )
    with op.batch_alter_table("notificaciones", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_notificaciones_usuario_id"), ["usuario_id"], unique=False)

    op.create_table(
        "visitas",
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("establecimiento_id", sa.Integer(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["establecimiento_id"], ["establecimientos.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("usuario_id", "establecimiento_id"),
    )
    with op.batch_alter_table("visitas", schema=None) as batch_op:
        batch_op.create_index(
            batch_op.f("ix_visitas_establecimiento_id"), ["establecimiento_id"], unique=False
        )

    op.create_table(
        "utiles",
        sa.Column("usuario_id", sa.Integer(), nullable=False),
        sa.Column("comentario_id", sa.Integer(), nullable=False),
        sa.Column("creado_en", sa.DateTime(timezone=True), nullable=False),
        sa.ForeignKeyConstraint(["comentario_id"], ["comentarios.id"], ondelete="CASCADE"),
        sa.ForeignKeyConstraint(["usuario_id"], ["usuarios.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("usuario_id", "comentario_id"),
    )
    with op.batch_alter_table("utiles", schema=None) as batch_op:
        batch_op.create_index(batch_op.f("ix_utiles_comentario_id"), ["comentario_id"], unique=False)


def downgrade() -> None:
    with op.batch_alter_table("utiles", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_utiles_comentario_id"))

    op.drop_table("utiles")
    with op.batch_alter_table("visitas", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_visitas_establecimiento_id"))

    op.drop_table("visitas")
    with op.batch_alter_table("notificaciones", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_notificaciones_usuario_id"))

    op.drop_table("notificaciones")
    with op.batch_alter_table("likes", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_likes_establecimiento_id"))

    op.drop_table("likes")
    with op.batch_alter_table("imagenes", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_imagenes_establecimiento_id"))

    op.drop_table("imagenes")
    with op.batch_alter_table("favoritos", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_favoritos_establecimiento_id"))

    op.drop_table("favoritos")
    op.drop_table("consultas")
    with op.batch_alter_table("comentarios", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_comentarios_establecimiento_id"))

    op.drop_table("comentarios")
    with op.batch_alter_table("actividades", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_actividades_usuario_id"))
        batch_op.drop_index(batch_op.f("ix_actividades_creado_en"))

    op.drop_table("actividades")
    with op.batch_alter_table("seguimientos", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_seguimientos_seguido_id"))

    op.drop_table("seguimientos")
    with op.batch_alter_table("mensajes", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_mensajes_creado_en"))

    op.drop_table("mensajes")
    with op.batch_alter_table("establecimientos", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_establecimientos_nombre"))

    op.drop_table("establecimientos")
    with op.batch_alter_table("usuarios", schema=None) as batch_op:
        batch_op.drop_index(batch_op.f("ix_usuarios_username"))
        batch_op.drop_index(batch_op.f("ix_usuarios_email"))

    op.drop_table("usuarios")
