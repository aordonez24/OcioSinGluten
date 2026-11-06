"""Pruebas de extremo a extremo: arrancan la aplicación real (backend + frontend compilado) con
datos de demostración en una base de datos temporal y la manejan con un navegador.

Requisitos: `npm run build` en frontend/ y `pip install -r e2e/requirements.txt`.
Por defecto usa el Chromium de Playwright (`playwright install chromium`); para usar otro
navegador instalado: E2E_NAVEGADOR=msedge (o chrome).
"""

import os
import socket
import subprocess
import sys
import tempfile
import time
from pathlib import Path
from urllib.parse import urlparse

import httpx
import pytest
from playwright.sync_api import sync_playwright

RAIZ = Path(__file__).resolve().parent.parent
BACKEND = RAIZ / "backend"
DIST = RAIZ / "frontend" / "dist"
# Sin SMTP, la app guarda aquí los correos que envía (ficheros .eml) y las pruebas los leen.
CORREOS = Path(tempfile.mkdtemp(prefix="osg-e2e-correos-"))


def _puerto_libre() -> int:
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


@pytest.fixture(scope="session")
def url_base():
    if not (DIST / "index.html").exists():
        pytest.exit("Compila antes el frontend: cd frontend && npm run build", returncode=1)

    carpeta = Path(tempfile.mkdtemp(prefix="osg-e2e-"))
    entorno = {
        **os.environ,
        "DATABASE_URL": f"sqlite:///{carpeta / 'e2e.db'}",
        "UPLOADS_DIR": str(carpeta / "uploads"),
        "FRONTEND_DIST": str(DIST),
        "GEOCODIFICAR": "false",
        "CORREOS_DIR": str(CORREOS),
        "SECRET_KEY": "clave-solo-para-pruebas-e2e",
        "PYTHONIOENCODING": "utf-8",
    }
    subprocess.run(
        [sys.executable, "-m", "app.cli", "demo"], cwd=BACKEND, env=entorno, check=True, capture_output=True
    )

    puerto = _puerto_libre()
    servidor = subprocess.Popen(
        [sys.executable, "-m", "uvicorn", "app.main:app", "--port", str(puerto), "--log-level", "warning"],
        cwd=BACKEND,
        env=entorno,
    )
    url = f"http://127.0.0.1:{puerto}"
    for _ in range(100):
        try:
            httpx.get(f"{url}/api/estadisticas", timeout=1)
            break
        except httpx.HTTPError:
            time.sleep(0.1)
    else:
        servidor.kill()
        pytest.exit("El servidor no arrancó", returncode=1)

    yield url
    servidor.terminate()
    servidor.wait(timeout=10)


@pytest.fixture(scope="session")
def navegador():
    with sync_playwright() as p:
        canal = os.environ.get("E2E_NAVEGADOR")
        nav = p.chromium.launch(channel=canal) if canal else p.chromium.launch()
        yield nav
        nav.close()


def _vigilar(page) -> list[str]:
    """Recoge errores de JavaScript y violaciones de la Content Security Policy."""
    errores = []
    page.on("pageerror", lambda e: errores.append(str(e)))
    page.on(
        "console",
        lambda m: m.type == "error" and "Content Security Policy" in m.text and errores.append(m.text),
    )
    return errores


@pytest.fixture
def pagina(navegador, url_base):
    """Página nueva (sin sesión) que falla el test si hay errores de JavaScript o de la CSP."""
    contexto = navegador.new_context(base_url=url_base, viewport={"width": 1280, "height": 860})
    page = contexto.new_page()
    errores = _vigilar(page)
    yield page
    contexto.close()
    assert not errores, f"Errores en el navegador: {errores}"


@pytest.fixture
def pagina_en_jaen(navegador, url_base):
    """Página con la ubicación del navegador simulada en el centro de Jaén."""
    contexto = navegador.new_context(
        base_url=url_base,
        viewport={"width": 1280, "height": 860},
        geolocation={"latitude": 37.7796, "longitude": -3.7849},
        permissions=["geolocation"],
    )
    page = contexto.new_page()
    errores = _vigilar(page)
    yield page
    contexto.close()
    assert not errores, f"Errores en el navegador: {errores}"


@pytest.fixture
def movil(navegador, url_base):
    contexto = navegador.new_context(
        base_url=url_base, viewport={"width": 390, "height": 844}, is_mobile=True, has_touch=True
    )
    page = contexto.new_page()
    errores = _vigilar(page)
    yield page
    contexto.close()
    assert not errores, f"Errores en el navegador: {errores}"


def en_ruta(ruta: str):
    """Para wait_for_url: compara solo la ruta (un patrón como "**/chat" también encajaría con
    "/entrar?volver=/chat" y el test seguiría antes de tiempo)."""
    return lambda url: urlparse(url).path == ruta


def entrar(page, email: str, password: str) -> None:
    page.goto("/entrar")
    page.fill("input[type=email]", email)
    page.fill("input[type=password]", password)
    page.click("form button.btn-primario")
    page.wait_for_url(en_ruta("/establecimientos"))


@pytest.fixture
def imagen(tmp_path) -> str:
    from PIL import Image

    ruta = tmp_path / "foto.jpg"
    Image.new("RGB", (1200, 800), "tan").save(ruta)
    return str(ruta)
