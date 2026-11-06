import re
import shutil
from io import BytesIO

import pytest
from PIL import Image

from app.config import get_settings
from conftest import crear_establecimiento, imagen_png, registrar

INDICE = """<!doctype html>
<html lang="es">
  <head>
    <meta name="description" content="Comunidad para encontrar sitios de ocio y restauración sin gluten." />
    <title>Ocio Sin Gluten</title>
  </head>
  <body><div id="app"></div></body>
</html>
"""


@pytest.fixture
def frontend():
    """Un index.html mínimo, como el que deja `npm run build` en frontend/dist."""
    carpeta = get_settings().frontend_dist
    carpeta.mkdir(parents=True, exist_ok=True)
    (carpeta / "index.html").write_text(INDICE, encoding="utf-8")
    yield
    shutil.rmtree(carpeta)


def test_vista_previa_de_un_establecimiento(client, frontend):
    h = registrar(client, "ana")
    est = crear_establecimiento(
        client, h, nombre="Dulce <Celíaca>", todo_sin_gluten=True, cocina_separada=True, categoria="panaderia"
    )
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios",
        json={"puntuacion": 5, "mensaje": "Genial"},
        headers=h,
    )
    client.post(
        f"/api/establecimientos/{est['id']}/imagenes", files={"archivo": ("a.png", imagen_png())}, headers=h
    ).json()

    r = client.get(f"/establecimientos/{est['id']}")
    assert r.status_code == 200 and r.headers["content-type"].startswith("text/html")
    html = r.text
    assert "<title>Dulce &lt;Celíaca&gt; - Ocio Sin Gluten</title>" in html  # escapado
    assert '<meta property="og:title" content="Dulce &lt;Celíaca&gt;" />' in html
    descripcion = "Panadería y pastelería en Jaén. 100 % sin gluten. Cocina separada. 5 de 5 en 1 opinión."
    assert f'<meta name="description" content="{descripcion}" />' in html
    imagen = re.search(
        r'<meta property="og:image" content="http://testserver(/vista-previa/\d+\.jpg)\?v=\w+" />', html
    )
    assert imagen and imagen[1] == f"/vista-previa/{est['id']}.jpg"
    assert '<meta property="og:image:width" content="1200" />' in html
    assert 'content="summary_large_image"' in html
    assert f'<link rel="canonical" href="http://testserver/establecimientos/{est["id"]}" />' in html
    assert html.count("<title>") == 1 and html.count('name="description"') == 1


def test_vista_previa_generica_y_paginas_privadas(client, frontend):
    inicio = client.get("/")
    assert inicio.status_code == 200
    assert "og:title" in inicio.text and "noindex" not in inicio.text

    provincia = client.get("/establecimientos?provincia=Granada")
    assert "<title>Sitios sin gluten en Granada - Ocio Sin Gluten</title>" in provincia.text

    # Los perfiles no se indexan (dicen quién es celíaco), aunque sí tienen vista previa
    registrar(client, "ana")
    perfil = client.get("/u/ana")
    assert "Perfil de Ana" in perfil.text and 'content="noindex"' in perfil.text
    assert 'content="noindex"' in client.get("/ajustes").text


def test_paginas_que_no_existen_responden_404(client, frontend):
    for ruta in ("/establecimientos/999", "/u/nadie"):
        r = client.get(ruta)
        assert r.status_code == 404, ruta
        assert '<div id="app">' in r.text  # la SPA muestra su propia página de "no encontrado"
    assert client.get("/api/no-existe").headers["content-type"] == "application/json"


def test_robots_y_sitemap(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h)

    robots = client.get("/robots.txt").text
    assert "Disallow: /api/" in robots and "Disallow: /u/" in robots
    assert "Sitemap: http://testserver/sitemap.xml" in robots

    sitemap = client.get("/sitemap.xml")
    assert sitemap.headers["content-type"] == "application/xml"
    assert f"<loc>http://testserver/establecimientos/{est['id']}</loc>" in sitemap.text
    assert "/u/" not in sitemap.text


def test_url_publica_en_las_vistas_previas(client, frontend, monkeypatch):
    monkeypatch.setattr(get_settings(), "url_publica", "https://ociosingluten.es/")
    assert 'content="https://ociosingluten.es/iconos/icono-512.png"' in client.get("/").text
    assert "Sitemap: https://ociosingluten.es/sitemap.xml" in client.get("/robots.txt").text


def test_tarjeta_para_compartir(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(
        client, h, nombre="Asador Restaurante La Cocina Sin Gluten de la Abuela Encarna", todo_sin_gluten=True
    )
    r = client.get(f"/vista-previa/{est['id']}.jpg")
    assert r.status_code == 200 and r.headers["content-type"] == "image/jpeg"
    assert Image.open(BytesIO(r.content)).size == (1200, 630)

    # Con foto, y la versión del enlace cambia al cambiar la valoración (las apps la vuelven a pedir)
    client.post(
        f"/api/establecimientos/{est['id']}/imagenes", files={"archivo": ("a.png", imagen_png())}, headers=h
    )
    assert client.get(f"/vista-previa/{est['id']}.jpg").status_code == 200
    assert client.get("/vista-previa/999.jpg").status_code == 404


def test_la_version_de_la_tarjeta_cambia_con_las_opiniones(client, frontend):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h)

    def version():
        return re.search(
            r"vista-previa/\d+\.jpg\?v=(\w+)", client.get(f"/establecimientos/{est['id']}").text
        )[1]

    antes = version()
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios", json={"puntuacion": 3, "mensaje": "Bien"}, headers=h
    )
    assert version() != antes
