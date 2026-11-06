"""Gestos del móvil (deslizar para recargar, visor de fotos), insignias y tarjeta para compartir."""

import re
from io import BytesIO

import httpx
from PIL import Image
from playwright.sync_api import expect

from conftest import entrar


def _deslizar(pagina, x0, y0, x1, y1, pasos=15):
    """Un dedo de verdad: eventos táctiles de Chrome (CDP), no clics del ratón."""
    cdp = pagina.context.new_cdp_session(pagina)
    cdp.send("Input.dispatchTouchEvent", {"type": "touchStart", "touchPoints": [{"x": x0, "y": y0}]})
    for i in range(1, pasos + 1):
        punto = {"x": x0 + (x1 - x0) * i / pasos, "y": y0 + (y1 - y0) * i / pasos}
        cdp.send("Input.dispatchTouchEvent", {"type": "touchMove", "touchPoints": [punto]})
        pagina.wait_for_timeout(16)
    cdp.send("Input.dispatchTouchEvent", {"type": "touchEnd", "touchPoints": []})


def test_deslizar_hacia_abajo_recarga_la_lista(movil):
    movil.goto("/establecimientos")
    expect(movil.locator(".tarjeta-est").first).to_be_visible()
    with movil.expect_request(lambda r: r.url.endswith("/api/establecimientos")):
        _deslizar(movil, 200, 480, 200, 760)
    expect(movil).to_have_url(re.compile(r"/establecimientos$"))  # no abre la tarjeta tocada

    # Un tirón corto no recarga
    peticiones = []
    movil.on("request", lambda r: "/api/establecimientos" in r.url and peticiones.append(r))
    _deslizar(movil, 200, 480, 200, 520)
    movil.wait_for_timeout(600)
    assert peticiones == []


def test_visor_de_fotos_con_el_dedo(movil):
    movil.goto("/establecimientos/2")
    movil.locator(".miniatura").first.click()
    contador = movil.locator(".visor .contador")
    expect(contador).to_have_text("1 de 2")
    _deslizar(movil, 320, 400, 80, 410)  # hacia la izquierda: la siguiente
    expect(contador).to_have_text("2 de 2")
    _deslizar(movil, 80, 400, 320, 410)  # hacia la derecha: la anterior
    expect(contador).to_have_text("1 de 2")
    _deslizar(movil, 200, 300, 205, 520)  # hacia abajo: se cierra
    expect(movil.locator(".visor")).to_have_count(0)


def test_insignias_del_perfil(pagina):
    entrar(pagina, "lucia@example.com", "Lucia1234")
    pagina.goto("/u/lucia")
    insignias = pagina.locator(".insignias")
    expect(insignias).to_contain_text("Llevas")
    expect(insignias.locator("li")).to_have_count(4)
    pagina.click("button:has-text('Ver las 7 insignias')")
    expect(insignias.locator("li")).to_have_count(7)
    expect(insignias.locator("li:not(.conseguida)").first).to_contain_text(" de ")  # con su progreso

    # En el perfil de otra persona solo se ven las conseguidas
    pagina.goto("/u/marcos")
    expect(pagina.locator(".insignias li").first).to_be_visible()
    expect(pagina.locator(".insignias li:not(.conseguida)")).to_have_count(0)
    expect(pagina.locator(".insignias")).not_to_contain_text("Llevas")


def test_tarjeta_para_compartir(url_base):
    html = httpx.get(f"{url_base}/establecimientos/2").text
    ruta = html.split('<meta property="og:image" content="')[1].split('"')[0]
    imagen = httpx.get(ruta)
    assert imagen.headers["content-type"] == "image/jpeg"
    assert Image.open(BytesIO(imagen.content)).size == (1200, 630)
