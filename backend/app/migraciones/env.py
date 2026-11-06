"""Entorno de Alembic: usa los modelos de la app y la DATABASE_URL de la configuración."""

from alembic import context

import app.models  # noqa: F401  (registra las tablas en Base.metadata)
from app.config import get_settings
from app.db import Base, engine_sin_claves_foraneas

config = context.config


def ejecutar(conexion) -> None:
    context.configure(
        connection=conexion,
        target_metadata=Base.metadata,
        # SQLite apenas sabe modificar columnas: Alembic recrea la tabla copiando los datos.
        render_as_batch=conexion.dialect.name == "sqlite",
        compare_type=True,
    )
    with context.begin_transaction():
        context.run_migrations()


if context.is_offline_mode():
    context.configure(url=get_settings().database_url, target_metadata=Base.metadata, literal_binds=True)
    with context.begin_transaction():
        context.run_migrations()
elif (conexion := config.attributes.get("connection")) is not None:
    ejecutar(conexion)
else:
    motor = engine_sin_claves_foraneas(get_settings().database_url)
    with motor.connect() as conexion:
        ejecutar(conexion)
    motor.dispose()
