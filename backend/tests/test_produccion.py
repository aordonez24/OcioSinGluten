"""Lo que importa al desplegar: fotos en Supabase Storage, comprobación de salud y límites contra el spam."""

import httpx
import pytest

from app.config import get_settings
from app.services import almacen, tarjeta
from conftest import crear_establecimiento, imagen_png, registrar

SUPABASE = "https://proyecto.supabase.co"


class StorageFalso:
    """Imita la API de Supabase Storage guardando los ficheros en memoria."""

    def __init__(self) -> None:
        self.ficheros: dict[str, bytes] = {}
        self.cabeceras: list[httpx.Headers] = []

    def post(self, url, *, content, headers, timeout):
        self.cabeceras.append(httpx.Headers(headers))
        self.ficheros[url.rsplit("/", 1)[1]] = content
        return httpx.Response(200, request=httpx.Request("POST", url))

    def get(self, url, *, timeout):
        nombre = url.rsplit("/", 1)[1]
        estado = 200 if nombre in self.ficheros else 404
        return httpx.Response(
            estado, content=self.ficheros.get(nombre, b""), request=httpx.Request("GET", url)
        )

    def request(self, metodo, url, *, json, headers, timeout):
        for nombre in json["prefixes"]:
            self.ficheros.pop(nombre, None)
        return httpx.Response(200, request=httpx.Request(metodo, url))


@pytest.fixture
def storage(monkeypatch):
    falso = StorageFalso()
    ajustes = get_settings()
    monkeypatch.setattr(ajustes, "supabase_url", SUPABASE)
    monkeypatch.setattr(ajustes, "supabase_secret_key", "sb_secret_prueba")
    for metodo in ("post", "get", "request"):
        monkeypatch.setattr(almacen.httpx, metodo, getattr(falso, metodo))
    tarjeta.dibujar.cache_clear()
    return falso


def test_fotos_en_supabase_storage(client, storage):
    h = registrar(client, "ana")
    url = f"/api/establecimientos/{crear_establecimiento(client, h)['id']}"
    r = client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h)
    assert r.status_code == 201

    imagen = r.json()
    nombre = imagen["url"].rsplit("/", 1)[1]
    assert imagen["url"] == f"{SUPABASE}/storage/v1/object/public/fotos/{nombre}"
    assert storage.ficheros[nombre][:4] == b"RIFF"  # WebP
    # Las claves nuevas no son JWT: solo van en la cabecera apikey
    assert storage.cabeceras[0]["apikey"] == "sb_secret_prueba"
    assert "authorization" not in storage.cabeceras[0]

    # La tarjeta para compartir dibuja la foto descargándola de Supabase
    assert client.get(f"/vista-previa/{url.rsplit('/', 1)[1]}.jpg").status_code == 200

    # La web permite cargar las fotos de Supabase y el resto de dominios sigue bloqueado
    csp = client.get("/api/estadisticas").headers["content-security-policy"]
    assert f"img-src 'self' data: blob: https://*.tile.openstreetmap.org {SUPABASE}" in csp
    assert f"wss://testserver {SUPABASE}" in csp

    assert client.delete(f"{url}/imagenes/{imagen['id']}", headers=h).status_code == 204
    assert storage.ficheros == {}


def test_si_supabase_falla_la_subida_se_avisa(client, storage, monkeypatch):
    def caido(url, **_):
        raise httpx.ConnectError("sin conexión")

    monkeypatch.setattr(almacen.httpx, "post", caido)
    h = registrar(client, "ana")
    url = f"/api/establecimientos/{crear_establecimiento(client, h)['id']}"
    r = client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h)
    assert r.status_code == 503
    assert client.get(url).json()["imagenes"] == []


def test_sin_supabase_las_fotos_van_a_disco(client):
    h = registrar(client, "ana")
    r = client.put(
        "/api/usuarios/yo/foto", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h
    )
    foto = r.json()["foto_url"]
    assert foto.startswith("/uploads/")
    assert (get_settings().uploads_dir / foto.removeprefix("/uploads/")).is_file()
    assert "supabase" not in client.get("/api/estadisticas").headers["content-security-policy"]


def test_salud(client):
    assert client.get("/api/salud").json() == {"ok": True}


def test_limites_contra_el_spam(client):
    consulta = {"nombre": "Ana", "email": "ana@example.com", "mensaje": "Hola, una duda"}
    estados = [client.post("/api/consultas", json=consulta).status_code for _ in range(6)]
    assert estados == [201] * 5 + [429]

    h = registrar(client, "ana")
    url = f"/api/establecimientos/{crear_establecimiento(client, h)['id']}/avisos"
    aviso = {"motivo": "cerrado", "mensaje": "Ha cerrado"}
    estados = [client.post(url, json=aviso, headers=h).status_code for _ in range(11)]
    assert estados == [201] * 10 + [429]


def test_el_chat_frena_los_mensajes_muy_seguidos(client):
    token = registrar(client, "ana")["Authorization"].split()[1]
    with client.websocket_connect(f"/api/chat?token={token}") as ws:
        ws.receive_json()  # historial
        for i in range(6):
            ws.send_json({"texto": f"mensaje {i}"})
        tipos = [ws.receive_json()["tipo"] for _ in range(6)]
    assert tipos == ["mensaje"] * 5 + ["aviso"]
