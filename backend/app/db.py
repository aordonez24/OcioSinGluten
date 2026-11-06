from collections.abc import Iterator
from pathlib import Path

from sqlalchemy import create_engine, event, inspect
from sqlalchemy.orm import DeclarativeBase, Session, sessionmaker

from app.config import get_settings

MIGRACIONES = Path(__file__).resolve().parent / "migraciones"
# Última revisión de antes de usar Alembic: las bases de datos de entonces tienen ya este esquema.
REVISION_INICIAL = "0001"


class Base(DeclarativeBase):
    pass


def _es_sqlite(url: str) -> bool:
    return url.startswith("sqlite")


def crear_engine(url: str):
    engine = create_engine(url, connect_args={"check_same_thread": False} if _es_sqlite(url) else {})
    if _es_sqlite(url):
        # SQLite no aplica las claves foráneas (ni los ON DELETE CASCADE) si no se activan.
        @event.listens_for(engine, "connect")
        def _pragmas(conexion, _):
            cursor = conexion.cursor()
            cursor.execute("PRAGMA foreign_keys=ON")
            cursor.execute("PRAGMA journal_mode=WAL")
            cursor.close()

    return engine


def engine_sin_claves_foraneas(url: str):
    """Engine para las migraciones. En SQLite, para cambiar una columna Alembic copia la tabla y borra
    la original; con las claves foráneas activas, ese borrado arrastraría en cascada las filas que
    dependen de ella (opiniones, fotos…)."""
    return create_engine(url)


def migrar(url: str | None = None) -> None:
    """Deja la base de datos en la última versión del esquema. Se puede llamar siempre: si ya está
    al día no hace nada."""
    from alembic import command
    from alembic.config import Config

    url = url or get_settings().database_url
    config = Config()
    config.set_main_option("script_location", str(MIGRACIONES))
    motor = engine_sin_claves_foraneas(url)
    try:
        with motor.begin() as conexion:
            config.attributes["connection"] = conexion
            tablas = set(inspect(conexion).get_table_names())
            if "usuarios" in tablas and "alembic_version" not in tablas:
                command.stamp(config, REVISION_INICIAL)  # creada antes de usar migraciones
            command.upgrade(config, "head")
    finally:
        motor.dispose()


engine = crear_engine(get_settings().database_url)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


def get_db() -> Iterator[Session]:
    with SessionLocal() as db:
        yield db
