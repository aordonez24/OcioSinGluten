"""Las migraciones de Alembic dejan la base de datos igual que los modelos y no pierden datos."""

from alembic import command
from alembic.autogenerate import compare_metadata
from alembic.config import Config
from alembic.migration import MigrationContext
from sqlalchemy import create_engine, text

from app.db import MIGRACIONES, Base, migrar


def test_el_esquema_migrado_coincide_con_los_modelos(tmp_path):
    url = f"sqlite:///{tmp_path / 'nueva.db'}"
    migrar(url)
    migrar(url)  # una segunda vez no hace nada
    with create_engine(url).connect() as conexion:
        contexto = MigrationContext.configure(conexion, opts={"compare_type": True})
        assert compare_metadata(contexto, Base.metadata) == []


def test_una_bd_anterior_a_las_migraciones_se_actualiza_sin_perder_datos(tmp_path):
    url = f"sqlite:///{tmp_path / 'antigua.db'}"
    motor = create_engine(url)
    # Esquema de la primera versión, sin la tabla alembic_version (como las creadas con create_all)
    config = Config()
    config.set_main_option("script_location", str(MIGRACIONES))
    with motor.begin() as conexion:
        config.attributes["connection"] = conexion
        command.upgrade(config, "0001")
        conexion.execute(text("DROP TABLE alembic_version"))
        conexion.execute(
            text(
                "INSERT INTO usuarios (id, username, email, dni, password_hash, nombre, apellidos,"
                " fecha_nacimiento, telefono, rol, archivado, creado_en) VALUES (1, 'ana', 'a@e.es',"
                " '12345678Z', 'x', 'Ana', 'P', '1990-01-01', '600000000', 'COMUN', 0, '2025-01-01')"
            )
        )
        conexion.execute(
            text(
                "INSERT INTO establecimientos (id, nombre, telefono, calle, cod_postal, localidad, provincia,"
                " pais, categoria, todo_sin_gluten, carta_sin_gluten, cocina_separada, acreditado, horario,"
                " creado_en) VALUES (1, 'Sitio', '953000000', 'C', '23002', 'Jaén', 'Jaén', 'España',"
                " 'BAR', 0, 0, 0, 0, 'De lunes a viernes', '2025-01-01')"
            )
        )
        conexion.execute(
            text(
                "INSERT INTO comentarios (establecimiento_id, autor_id, puntuacion, mensaje, creado_en)"
                " VALUES (1, 1, 5, 'Muy bien', '2025-01-02')"
            )
        )
    motor.dispose()

    migrar(url)

    with create_engine(url).connect() as conexion:
        fila = conexion.execute(text("SELECT nota_horario, horario FROM establecimientos")).one()
        assert tuple(fila) == ("De lunes a viernes", None)  # el texto antiguo pasa a ser la nota
        # Recrear la tabla de establecimientos no ha borrado en cascada sus opiniones
        assert conexion.execute(text("SELECT count(*) FROM comentarios")).scalar() == 1
