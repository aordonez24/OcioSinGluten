"""Seguridad de la cuenta, descargar los datos y vista previa de los enlaces compartidos."""

import json
import re
from pathlib import Path

import httpx
from playwright.sync_api import expect

from conftest import en_ruta, entrar


def _registrar(url_base: str, username: str, numero: int) -> str:
    """Crea una cuenta nueva por la API (así la prueba no cambia la contraseña de la demo)."""
    dni = f"{numero:08d}{'TRWAGMYFPDXBNJZSQVHLCKE'[numero % 23]}"
    datos = {
        "dni": dni,
        "username": username,
        "nombre": username.title(),
        "apellidos": "Pruebas",
        "fecha_nacimiento": "1990-01-01",
        "telefono": "600123123",
        "email": f"{username}@example.com",
        "password": "Secreta123",
    }
    r = httpx.post(f"{url_base}/api/auth/registro", json=datos)
    assert r.status_code == 201, r.text
    return datos["email"]


def test_cambiar_la_contrasena_cierra_las_demas_sesiones(pagina, navegador, url_base):
    correo = _registrar(url_base, "nora", 31415926)
    otro_dispositivo = navegador.new_context(base_url=url_base).new_page()
    entrar(otro_dispositivo, correo, "Secreta123")
    entrar(pagina, correo, "Secreta123")

    pagina.goto("/ajustes")
    pagina.fill("label:has-text('Contraseña actual') input", "Secreta123")
    pagina.fill("label:has-text('Nueva contraseña') input", "Distinta123")
    pagina.fill("label:has-text('Repítela') input", "Distinta123")
    pagina.click("button:has-text('Cambiar contraseña')")
    expect(pagina.locator(".aviso")).to_contain_text("Se ha cerrado la sesión en tus otros dispositivos")

    # Aquí la sesión sigue abierta...
    pagina.goto("/comunidad")
    pagina.wait_for_url(en_ruta("/comunidad"))
    expect(pagina.locator("h1")).to_be_visible()
    # ...y en el otro dispositivo se cierra: lo explica y lleva a "Iniciar sesión"
    otro_dispositivo.goto("/ajustes")
    otro_dispositivo.wait_for_url(en_ruta("/entrar"))
    expect(otro_dispositivo.locator(".aviso")).to_contain_text("Tu sesión se ha cerrado")
    entrar(otro_dispositivo, correo, "Distinta123")
    otro_dispositivo.context.close()


def test_descargar_mis_datos(pagina, tmp_path: Path):
    entrar(pagina, "marcos@example.com", "Marcos1234")
    pagina.goto("/ajustes")
    with pagina.expect_download() as descarga:
        pagina.click("button:has-text('Descargar mis datos')")
    assert descarga.value.suggested_filename == "ociosingluten-marcos.json"
    ruta = tmp_path / "datos.json"
    descarga.value.save_as(ruta)
    datos = json.loads(ruta.read_text(encoding="utf-8"))
    assert datos["cuenta"]["email"] == "marcos@example.com"
    assert datos["opiniones"], "marcos tiene opiniones en la demo"
    assert "password_hash" not in ruta.read_text(encoding="utf-8")


def test_vista_previa_al_compartir_un_sitio(pagina, url_base):
    html = httpx.get(f"{url_base}/establecimientos/2").text
    assert '<meta property="og:title" content="Horno Sin Trigo" />' in html
    assert re.search(r'<meta property="og:image" content="http://[^"]+/uploads/[^"]+\.webp" />', html)
    assert "<title>Horno Sin Trigo - Ocio Sin Gluten</title>" in html

    # La página sigue funcionando igual (y sin errores de la CSP, que vigila el fixture)
    pagina.goto("/establecimientos/2")
    expect(pagina.locator("h1")).to_have_text("Horno Sin Trigo")
    expect(pagina).to_have_title("Horno Sin Trigo - Ocio Sin Gluten")

    assert "Disallow: /u/" in httpx.get(f"{url_base}/robots.txt").text
    assert "/establecimientos/2</loc>" in httpx.get(f"{url_base}/sitemap.xml").text


def test_filtros_caben_en_un_movil_estrecho(movil):
    for ancho in (360, 390):
        movil.set_viewport_size({"width": ancho, "height": 800})
        movil.goto("/establecimientos")
        buscador = movil.locator(".filtros .buscador").bounding_box()
        filtros = movil.locator(".filtros .mostrar-filtros").bounding_box()
        assert abs(buscador["y"] - filtros["y"]) < 2, f"'Filtros' baja de línea a {ancho} px"
        sobra = movil.evaluate("() => document.documentElement.scrollWidth - window.innerWidth")
        assert sobra <= 0, f"La página se sale {sobra} px por la derecha a {ancho} px"
