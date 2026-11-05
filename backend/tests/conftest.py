import os
import tempfile
from io import BytesIO
from pathlib import Path

# Configuración de pruebas: BD y carpeta de subidas temporales, sin llamadas a internet.
_tmp = Path(tempfile.mkdtemp(prefix="osg-tests-"))
os.environ["DATABASE_URL"] = f"sqlite:///{_tmp / 'test.db'}"
os.environ["UPLOADS_DIR"] = str(_tmp / "uploads")
os.environ["GEOCODIFICAR"] = "false"
os.environ["FRONTEND_DIST"] = str(_tmp / "no-existe")

import pytest  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402
from PIL import Image  # noqa: E402

from app.db import Base, engine  # noqa: E402
from app.main import app  # noqa: E402

DNIS = ["12345678Z", "87654321X", "11111111H", "22222222J"]


@pytest.fixture
def client():
    from app.services import limite

    limite.reiniciar()
    Base.metadata.drop_all(engine)
    Base.metadata.create_all(engine)
    with TestClient(app) as c:
        yield c


def registrar(client, username: str, n: int = 0, **extra) -> dict:
    """Registra un usuario y devuelve las cabeceras de autenticación."""
    datos = {
        "dni": DNIS[n],
        "username": username,
        "nombre": username.title(),
        "apellidos": "Pruebas",
        "fecha_nacimiento": "1990-01-01",
        "telefono": "600123123",
        "email": f"{username}@example.com",
        "password": "Secreta123",
        **extra,
    }
    r = client.post("/api/auth/registro", json=datos)
    assert r.status_code == 201, r.text
    return {"Authorization": f"Bearer {r.json()['access_token']}"}


def hacer_admin(username: str) -> None:
    from sqlalchemy import update

    from app.db import SessionLocal
    from app.models import Rol, Usuario

    with SessionLocal() as db:
        db.execute(update(Usuario).where(Usuario.username == username).values(rol=Rol.ADMIN))
        db.commit()


def crear_establecimiento(client, headers, **extra) -> dict:
    datos = {
        "nombre": "Celiaco Feliz",
        "telefono": "953000000",
        "calle": "Calle Maestra 10",
        "cod_postal": "23002",
        "localidad": "Jaén",
        "provincia": "Jaén",
        **extra,
    }
    r = client.post("/api/establecimientos", json=datos, headers=headers)
    assert r.status_code == 201, r.text
    return r.json()


def imagen_png() -> bytes:
    buffer = BytesIO()
    Image.new("RGB", (2000, 1000), "orange").save(buffer, "PNG")
    return buffer.getvalue()
