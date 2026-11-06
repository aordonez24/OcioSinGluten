"""Horario con "abierto ahora", recuperar la contraseña por email y uso sin conexión."""

import email
import re
from urllib.parse import urlparse

from playwright.sync_api import expect

from conftest import CORREOS, en_ruta, entrar

def esperar(pagina, funcion_js: str, segundos: float = 5) -> None:
    """Como wait_for_function, pero esperando a las promesas (wait_for_function las da por buenas)."""
    for _ in range(int(segundos * 10)):
        if pagina.evaluate(funcion_js):
            return
        pagina.wait_for_timeout(100)
    raise AssertionError(f"No se cumplió: {funcion_js}")


# Lunes 9 de noviembre de 2026 a las 13:30 en la España peninsular
LUNES_1330 = "2026-11-09T13:30:00+01:00"


def test_abierto_ahora(pagina):
    pagina.clock.set_fixed_time(LUNES_1330)
    pagina.goto("/establecimientos?q=celiaco feliz")
    # Celiaco Feliz cierra los lunes
    expect(pagina.locator(".tarjeta-est .estado")).to_have_text("Cerrado, abre mañana a las 13:00")

    pagina.goto("/establecimientos?abierto=1")
    nombres = pagina.locator(".tarjeta-est h3")
    expect(nombres).to_have_count(5)
    expect(nombres).not_to_contain_text(["Celiaco Feliz"])
    expect(pagina.locator(".tarjeta-est", has_text="Burger Zero").locator(".estado")).to_have_text(
        "Abierto hasta las 23:30"
    )
    # El filtro se quita como los demás
    pagina.click(".filtros button:has-text('Abierto ahora')")
    expect(nombres.filter(has_text="Celiaco Feliz")).to_have_count(1)

    # En la ficha, el horario de la semana con el día de hoy marcado
    pagina.click("text=Horno Sin Trigo")
    expect(pagina.locator(".datos .estado")).to_have_text("Cierra pronto, a las 14:00")
    pagina.click("summary:has-text('Horario de la semana')")
    expect(pagina.locator(".semana tr.hoy")).to_contain_text("Lunes")
    expect(pagina.locator(".semana tr:has-text('Domingo')")).to_contain_text("Cerrado")


def _ultimo_enlace(destino: str) -> str:
    for fichero in sorted(CORREOS.glob("*.eml"), key=lambda f: f.stat().st_mtime, reverse=True):
        mensaje = email.message_from_bytes(fichero.read_bytes())
        if mensaje["To"] == destino:
            texto = mensaje.get_payload(decode=True).decode()
            enlace = urlparse(re.search(r"https?://\S+", texto).group(0))
            return f"{enlace.path}?{enlace.query}"
    raise AssertionError(f"No se ha enviado ningún correo a {destino}")


def test_recuperar_la_contrasena(pagina):
    pagina.goto("/entrar")
    pagina.fill("input[type=email]", "javi@example.com")
    pagina.click("text=¿Has olvidado la contraseña?")
    expect(pagina.locator("input[type=email]")).to_have_value("javi@example.com")
    pagina.click("button:has-text('Enviar el enlace')")
    expect(pagina.locator("h1")).to_have_text("Revisa tu correo")

    enlace = _ultimo_enlace("javi@example.com")
    pagina.goto(enlace)
    expect(pagina).to_have_url(re.compile(r"/restablecer$"))  # el token no se queda en la barra
    pagina.fill("label:has-text('Contraseña nueva') input", "Nueva12345")
    pagina.fill("label:has-text('Repite la contraseña') input", "Otra12345")
    pagina.click("button:has-text('Guardar y entrar')")
    expect(pagina.locator(".error-campo:not(:empty)")).to_have_text("Las contraseñas no coinciden")
    pagina.fill("label:has-text('Repite la contraseña') input", "Nueva12345")
    pagina.click("button:has-text('Guardar y entrar')")
    pagina.wait_for_url(en_ruta("/establecimientos"))
    expect(pagina.locator(".aviso")).to_contain_text("Contraseña cambiada")

    # El mismo enlace ya no vale
    pagina.goto(enlace)
    pagina.fill("label:has-text('Contraseña nueva') input", "Otra123456")
    pagina.fill("label:has-text('Repite la contraseña') input", "Otra123456")
    pagina.click("button:has-text('Guardar y entrar')")
    expect(pagina.locator("h1")).to_have_text("Este enlace ya no sirve")


def test_sin_conexion(pagina):
    pagina.goto("/establecimientos")
    pagina.wait_for_function("() => navigator.serviceWorker.controller !== null")
    pagina.reload()  # desde aquí las respuestas pasan por el service worker y se guardan
    tarjetas = pagina.locator(".tarjeta-est")
    expect(tarjetas.first).to_be_visible()
    total = tarjetas.count()
    pagina.click("text=Celiaco Feliz")
    expect(pagina.locator("h1")).to_have_text("Celiaco Feliz")
    expect(pagina.locator(".opinion").first).to_be_visible()

    pagina.context.set_offline(True)
    try:
        pagina.reload()
        expect(pagina.locator("h1")).to_have_text("Celiaco Feliz")
        expect(pagina.locator(".sin-conexion")).to_contain_text("Sin conexión")
        pagina.click("text=Todos los establecimientos")
        expect(tarjetas).to_have_count(total)
        # Lo que nunca se abrió no está guardado: se explica en vez de quedarse cargando
        pagina.goto("/u/marcos")
        expect(pagina.locator("body")).to_contain_text(re.compile("conexión|no existe|No hay", re.I))
    finally:
        pagina.context.set_offline(False)


def test_cerrar_sesion_borra_las_copias(pagina):
    """Las copias sin conexión pueden llevar datos de la cuenta: no deben sobrevivir a la sesión."""
    entrar(pagina, "lucia@example.com", "Lucia1234")
    pagina.wait_for_function("() => navigator.serviceWorker.controller !== null")
    pagina.goto("/u/lucia")
    esperar(pagina, "() => caches.has('osg-datos')")
    pagina.click(".disparador")
    pagina.click("[role=menuitem]:has-text('Cerrar sesión')")
    esperar(pagina, "async () => !(await caches.has('osg-datos'))")
