"""Recuperar la contraseña con un enlace enviado por email."""

import email
import re
from pathlib import Path

from app.config import get_settings
from conftest import registrar


def _correos() -> list[Path]:
    carpeta = get_settings().correos_dir
    return sorted(carpeta.glob("*.eml"), key=lambda f: f.stat().st_mtime) if carpeta.exists() else []


def _pedir_enlace(client, correo: str = "ana@example.com") -> str:
    antes = set(_correos())
    r = client.post("/api/auth/recuperar", json={"email": correo})
    assert r.status_code == 202
    [nuevo] = set(_correos()) - antes
    mensaje = email.message_from_bytes(nuevo.read_bytes())
    assert mensaje["To"] == "ana@example.com"
    return re.search(r"token=(\S+)", mensaje.get_payload(decode=True).decode()).group(1)


def test_recuperar_y_restablecer(client):
    registrar(client, "ana")
    token = _pedir_enlace(client, "  ANA@example.com ")

    # El enlace no sirve como sesión
    assert client.get("/api/auth/yo", headers={"Authorization": f"Bearer {token}"}).status_code == 401
    # La contraseña nueva cumple las mismas reglas que al registrarse
    assert client.post("/api/auth/restablecer", json={"token": token, "password": "corta"}).status_code == 422

    r = client.post("/api/auth/restablecer", json={"token": token, "password": "Nueva12345"})
    assert r.status_code == 200 and r.json()["usuario"]["username"] == "ana"
    login = {"email": "ana@example.com", "password": "Nueva12345"}
    assert client.post("/api/auth/login", json=login).status_code == 200

    # Cada enlace sirve una sola vez
    r = client.post("/api/auth/restablecer", json={"token": token, "password": "Otra123456"})
    assert r.status_code == 400


def test_un_cambio_de_contrasena_invalida_los_enlaces_anteriores(client):
    h = registrar(client, "ana")
    token = _pedir_enlace(client)
    cambio = {"actual": "Secreta123", "nueva": "Distinta123"}
    assert client.put("/api/usuarios/yo/password", json=cambio, headers=h).status_code == 200
    r = client.post("/api/auth/restablecer", json={"token": token, "password": "Nueva12345"})
    assert r.status_code == 400


def test_no_revela_que_cuentas_existen(client):
    antes = set(_correos())
    r = client.post("/api/auth/recuperar", json={"email": "nadie@example.com"})
    assert r.status_code == 202 and r.json() == {"ok": True}
    assert set(_correos()) == antes  # no se envía nada


def test_un_token_de_sesion_no_sirve_para_restablecer(client):
    h = registrar(client, "ana")
    sesion = h["Authorization"].removeprefix("Bearer ")
    r = client.post("/api/auth/restablecer", json={"token": sesion, "password": "Nueva12345"})
    assert r.status_code == 400
    r = client.post("/api/auth/restablecer", json={"token": "basura", "password": "Nueva12345"})
    assert r.status_code == 400


def test_limite_de_peticiones(client):
    for _ in range(5):
        assert client.post("/api/auth/recuperar", json={"email": "x@example.com"}).status_code == 202
    assert client.post("/api/auth/recuperar", json={"email": "x@example.com"}).status_code == 429
