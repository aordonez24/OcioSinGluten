"""Lo que importa al desplegar: fotos sin disco, comprobación de salud y límites contra el spam."""

from sqlalchemy import func, select

from app.config import Settings, get_settings
from app.db import SessionLocal
from app.models import Foto
from app.services import tarjeta
from conftest import crear_establecimiento, imagen_png, registrar


def test_las_fotos_se_guardan_en_la_base_de_datos(client):
    h = registrar(client, "ana")
    est_id = crear_establecimiento(client, h)["id"]
    url = f"/api/establecimientos/{est_id}"
    r = client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h)
    imagen = r.json()
    nombre = imagen["url"].removeprefix("/uploads/")

    # No se escribe nada en disco: el servidor puede no tener disco persistente (Render)
    assert not get_settings().uploads_dir.joinpath(nombre).exists()
    foto = client.get(imagen["url"])
    assert foto.status_code == 200 and foto.content[:4] == b"RIFF"
    assert foto.headers["content-type"] == "image/webp"
    assert "immutable" in foto.headers["cache-control"]
    assert client.get("/uploads/no-existe.webp").status_code == 404

    tarjeta.dibujar.cache_clear()  # la tarjeta para compartir dibuja la foto
    assert client.get(f"/vista-previa/{est_id}.jpg").status_code == 200

    # Al borrar la imagen o el sitio, la foto desaparece en la misma transacción
    client.delete(f"{url}/imagenes/{imagen['id']}", headers=h)
    assert client.get(imagen["url"]).status_code == 404
    with SessionLocal() as db:
        assert db.scalar(select(func.count()).select_from(Foto)) == 0


def test_la_foto_de_perfil_anterior_se_borra(client):
    h = registrar(client, "ana")
    subir = lambda: client.put(  # noqa: E731
        "/api/usuarios/yo/foto", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h
    ).json()["foto_url"]
    primera, segunda = subir(), subir()
    assert client.get(primera).status_code == 404 and client.get(segunda).status_code == 200
    client.delete("/api/usuarios/ana", headers=h)
    assert client.get(segunda).status_code == 404


def test_url_publica_desde_render(monkeypatch):
    monkeypatch.setenv("RENDER_EXTERNAL_URL", "https://ociosingluten.onrender.com")
    monkeypatch.delenv("URL_PUBLICA", raising=False)
    assert Settings().url_publica == "https://ociosingluten.onrender.com"


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


def test_descargar_la_app_de_android(client):
    r = client.get("/descargas/ociosingluten.apk")
    assert r.status_code == 200
    assert r.headers["content-type"] == "application/vnd.android.package-archive"
    assert r.content[:2] == b"PK"  # un APK es un zip
