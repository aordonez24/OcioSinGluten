"""Recorridos completos como los haría una persona usando la aplicación."""

import re

from playwright.sync_api import expect

from conftest import en_ruta, entrar


def test_portada_y_busqueda(pagina):
    pagina.goto("/")
    expect(pagina.locator(".cifras")).to_contain_text("sitios sin gluten")
    pagina.fill(".buscador-hero input", "granada")
    pagina.press(".buscador-hero input", "Enter")
    pagina.wait_for_url(re.compile(r"/establecimientos\?q=granada"))
    expect(pagina.locator(".tarjeta-est")).to_have_count(1)
    expect(pagina.locator(".tarjeta-est")).to_contain_text("Horno Sin Trigo")


def test_explorar_filtros_en_la_url_y_mapa(pagina):
    pagina.goto("/establecimientos")
    pagina.fill(".filtros input[type=search]", "jaen")  # sin tilde
    expect(pagina.locator(".tarjeta-est")).to_have_count(2)
    pagina.click(".distintivos button:has-text('100 % sin gluten')")
    expect(pagina).to_have_url(re.compile(r"con=todo_sin_gluten"))
    expect(pagina.locator(".tarjeta-est")).to_have_count(1)

    pagina.reload()  # los filtros sobreviven a recargar la página
    expect(pagina.locator(".tarjeta-est")).to_have_count(1)

    pagina.click("button:has-text('Quitar filtros')")
    pagina.click("button:has-text('Mapa')")
    expect(pagina.locator(".leaflet-interactive")).to_have_count(8)


def test_registro_valida_y_crea_la_cuenta(pagina, imagen):
    pagina.goto("/registro")
    pagina.click("form button.btn-primario")
    expect(pagina.locator(".error-campo", has_text="Entre 3 y 30")).to_be_visible()

    pagina.set_input_files(".avatar-boton input", imagen)
    pagina.fill("input[autocomplete=username]", "nuria")
    pagina.fill("input[type=email]", "nuria@example.com")
    claves = pagina.locator("input[autocomplete=new-password]")
    claves.nth(0).fill("Nuria1234")
    claves.nth(1).fill("Nuria1234")
    pagina.fill("input[autocomplete=given-name]", "Nuria")
    pagina.fill("input[autocomplete=family-name]", "Pérez")
    pagina.fill("input[placeholder='12345678Z']", "12345678A")
    pagina.locator("input[placeholder='12345678Z']").blur()
    expect(pagina.locator(".error-campo", has_text="DNI no válido")).to_be_visible()
    pagina.fill("input[placeholder='12345678Z']", "12345678Z")
    pagina.fill("input[autocomplete=tel]", "611222333")
    pagina.fill("input[type=date]", "1994-03-02")
    pagina.check("input[type=checkbox]")
    pagina.click("form button.btn-primario")

    pagina.wait_for_url(en_ruta("/establecimientos"))
    expect(pagina.locator(".disparador img")).to_be_visible()  # la foto de perfil ya está subida


def test_anadir_sitio_opinar_y_editar(pagina, imagen):
    entrar(pagina, "lucia@example.com", "Lucia1234")
    pagina.goto("/establecimientos/nuevo")
    pagina.fill("label:has-text('Nombre del establecimiento') input", "Café Celíaco")
    pagina.select_option("label:has-text('Tipo de sitio') select", "cafeteria")
    pagina.click("label.distintivo:has-text('Cocina separada')")
    pagina.fill("label:has-text('Teléfono') input", "953111222")
    pagina.select_option("label:has-text('Provincia') select", "Jaén")
    pagina.fill("label:has-text('Localidad') input", "Linares")
    pagina.fill("label:has-text('Dirección') input", "Calle Corredera 3")
    pagina.fill("label:has-text('Código postal') input", "23700")
    pagina.click("text=Publicar establecimiento")

    pagina.wait_for_url(re.compile(r"/establecimientos/\d+$"))
    expect(pagina.locator("h1")).to_have_text("Café Celíaco")
    expect(pagina.locator(".categoria")).to_contain_text("Cafetería")
    expect(pagina.locator(".distintivos")).to_contain_text("Cocina separada")

    # Acciones con respuesta inmediata
    pagina.click("button:has-text('Me gusta')")
    expect(pagina.locator("button:has-text('Me gusta · 1')")).to_be_visible()
    pagina.click("button:has-text('Guardar')")
    expect(pagina.locator("button:has-text('Guardado')")).to_be_visible()

    # Foto
    pagina.set_input_files("label:has-text('Añadir') input", imagen)
    expect(pagina.locator(".miniatura")).to_have_count(1)

    # Opinión con estrellas: obligatorio elegir puntuación
    pagina.fill(".nueva textarea", "Tostadas sin gluten buenísimas")
    pagina.click("button:has-text('Publicar opinión')")
    expect(pagina.locator(".aviso.error")).to_contain_text("estrellas")
    pagina.click(".selector label:nth-of-type(4)")
    pagina.click("button:has-text('Publicar opinión')")
    expect(pagina.locator(".opinion.propia")).to_contain_text("Tostadas sin gluten")
    expect(pagina.locator(".media strong")).to_have_text("4.0")
    expect(pagina.locator(".nueva")).to_have_count(0)  # solo una opinión por persona

    # Editarla
    pagina.click("button[title='Editar opinión']")
    pagina.click(".selector label:nth-of-type(2)")
    pagina.click(".nueva button:has-text('Guardar')")
    expect(pagina.locator(".media strong")).to_have_text("2.0")
    expect(pagina.locator(".opinion.propia")).to_contain_text("editada")

    # Borrarla pasa por el diálogo de confirmación
    pagina.click("button[title='Borrar opinión']")
    pagina.click("dialog button:has-text('Cancelar')")
    expect(pagina.locator(".opinion.propia")).to_have_count(1)
    pagina.click("button[title='Borrar opinión']")
    pagina.click("dialog button:has-text('Borrar')")
    expect(pagina.locator(".opinion.propia")).to_have_count(0)


def test_comunidad_perfil_y_chat(pagina):
    entrar(pagina, "marcos@example.com", "Marcos1234")
    pagina.goto("/comunidad")
    boton = pagina.locator(".usuario", has_text="@javi").locator("button")

    def pulsar_y_esperar(metodo):
        # El botón cambia al instante (respuesta optimista): se espera también al servidor.
        with pagina.expect_response(lambda r: "/seguir" in r.url and r.request.method == metodo):
            boton.click()

    if "Siguiendo" in boton.inner_text():  # en la demo los seguimientos son aleatorios
        pulsar_y_esperar("DELETE")
        expect(boton).to_have_text(re.compile("Seguir"))
    pulsar_y_esperar("PUT")
    expect(boton).to_have_text(re.compile("Siguiendo"))

    pagina.goto("/u/marcos?tab=seguidos")
    expect(pagina.locator(".usuario", has_text="@javi")).to_be_visible()

    pagina.goto("/chat")
    expect(pagina.locator(".chip", has_text="En línea")).to_be_visible()
    expect(pagina.locator(".burbuja").first).to_be_visible()  # historial guardado
    pagina.fill(".escribir input", "Hola desde las pruebas 👋")
    pagina.press(".escribir input", "Enter")
    expect(pagina.locator(".mio .texto", has_text="Hola desde las pruebas")).to_be_visible()


def test_sesion_ajustes_y_rutas_protegidas(pagina):
    pagina.goto("/chat")
    pagina.wait_for_url("**/entrar?volver=/chat")
    pagina.fill("input[type=email]", "sara@example.com")
    pagina.fill("input[type=password]", "Sara12345")
    pagina.click("form button.btn-primario")
    pagina.wait_for_url(en_ruta("/chat"))  # vuelve a donde quería ir

    pagina.goto("/ajustes")
    pagina.fill("label:has-text('Nombre de usuario') input", "sara_sg")
    pagina.click("button:has-text('Guardar cambios')")
    expect(pagina.locator(".disparador")).to_contain_text("sara_sg")

    pagina.click(".disparador")
    pagina.click("[role=menuitem]:has-text('Cerrar sesión')")
    expect(pagina.locator("text=Crear cuenta").first).to_be_visible()


def test_admin_modera_y_recibe_consultas(pagina):
    pagina.goto("/")
    pagina.fill("#contacto label:has-text('Nombre') input", "Pepe")
    pagina.fill("#contacto input[type=email]", "pepe@example.com")
    pagina.fill("#contacto textarea", "¿Podéis añadir más sitios en Huelva?")
    pagina.click("#contacto button")
    expect(pagina.locator("#contacto")).to_contain_text("Gracias")

    entrar(pagina, "admin@example.com", "Admin1234")
    pagina.goto("/consultas")
    expect(pagina.locator("blockquote", has_text="Huelva")).to_be_visible()

    pagina.goto("/u/javi")
    pagina.click(".moderacion button:has-text('Suspender')")
    pagina.click("dialog button:has-text('Suspender')")
    expect(pagina.locator(".chip", has_text="Cuenta suspendida")).to_be_visible()
    pagina.click(".moderacion button:has-text('Restaurar')")
    expect(pagina.locator(".chip", has_text="Cuenta suspendida")).to_have_count(0)


def test_navegacion_movil(movil):
    entrar(movil, "lucia@example.com", "Lucia1234")
    barra = movil.locator("nav.barra")
    expect(barra).to_be_visible()
    barra.locator("text=Perfil").click()
    movil.wait_for_url(en_ruta("/u/lucia"))
    barra.locator("text=Añadir").click()
    movil.wait_for_url(en_ruta("/establecimientos/nuevo"))
    # Comunidad y Actividad, que no caben en la barra, están en el menú del avatar
    movil.click(".disparador")
    movil.click("[role=menuitem]:has-text('Comunidad')")
    movil.wait_for_url(en_ruta("/comunidad"))


def test_cerca_de_mi_ordena_por_distancia(pagina_en_jaen):
    pagina_en_jaen.goto("/establecimientos")
    pagina_en_jaen.click("button:has-text('Cerca de mí')")
    expect(pagina_en_jaen).to_have_url(re.compile(r"orden=cerca"))
    tarjetas = pagina_en_jaen.locator(".tarjeta-est")
    # Desde el centro de Jaén lo más cercano es Celiaco Feliz y después Mesón La Loma (Úbeda)
    expect(tarjetas.nth(0)).to_contain_text("Celiaco Feliz")
    expect(tarjetas.nth(1)).to_contain_text("Mesón La Loma")
    expect(tarjetas.nth(0).locator(".distancia")).to_have_text(re.compile(r"a \d+(,\d)? k?m"))
    # Barcelona es lo más lejano; los sitios sin coordenadas (sin distancia) van detrás
    con_distancia = pagina_en_jaen.locator(".tarjeta-est:has(.distancia)")
    expect(con_distancia.nth(-1)).to_contain_text("Dulce Celíaca")


def test_detalle_carga_mapa_sin_violar_la_csp(pagina):
    pagina.goto("/establecimientos/1")
    expect(pagina.locator(".leaflet-container")).to_be_visible()
    expect(pagina.locator("a:has-text('Cómo llegar')")).to_have_attribute(
        "href", re.compile("google.com/maps")
    )
