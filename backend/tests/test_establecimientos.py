from conftest import crear_establecimiento, hacer_admin, imagen_png, registrar


def test_crear_y_listar(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h)
    assert est["num_likes"] == 0 and est["valoracion"] is None

    listado = client.get("/api/establecimientos").json()
    assert [e["nombre"] for e in listado] == ["Celiaco Feliz"]
    # El listado no incluye las imágenes: solo la URL de la portada.
    assert "imagenes" not in listado[0] and "portada_url" in listado[0]

    duplicado = client.post(
        "/api/establecimientos",
        json={
            "nombre": "Celiaco Feliz",
            "telefono": "953000000",
            "calle": "Otra",
            "cod_postal": "23002",
            "localidad": "Jaén",
            "provincia": "Jaén",
        },
        headers=h,
    )
    assert duplicado.status_code == 409


def test_like_favorito_visita_y_valoracion(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est_id = crear_establecimiento(client, h_ana)["id"]
    url = f"/api/establecimientos/{est_id}"

    for h in (h_ana, h_bea):
        client.put(f"{url}/visita", headers=h)
    client.put(f"{url}/like", headers=h_ana)
    client.put(f"{url}/like", headers=h_ana)  # un segundo like no cuenta doble
    client.put(f"{url}/favorito", headers=h_ana)

    detalle = client.get(url, headers=h_ana).json()
    assert (detalle["num_likes"], detalle["num_visitas"]) == (1, 2)
    assert detalle["valoracion"] == 2.5  # la mitad de quienes lo visitaron dieron like
    assert detalle["me_gusta"] and detalle["favorito"] and detalle["visitado"]
    assert client.get(url, headers=h_bea).json()["me_gusta"] is False

    assert [e["id"] for e in client.get("/api/usuarios/ana/favoritos").json()] == [est_id]
    assert len(client.get("/api/usuarios/bea/visitados").json()) == 1

    client.delete(f"{url}/like", headers=h_ana)
    assert client.get(url).json()["num_likes"] == 0
    tipos = [a["tipo"] for a in client.get("/api/actividades?usuario=ana").json()]
    assert "like" not in tipos and "visita" in tipos


def test_solo_admin_edita_y_borra(client):
    h_ana = registrar(client, "ana", 0)
    h_admin = registrar(client, "jefa", 1)
    hacer_admin("jefa")
    est_id = crear_establecimiento(client, h_ana)["id"]
    url = f"/api/establecimientos/{est_id}"

    assert client.patch(url, json={"nombre": "Nuevo"}, headers=h_ana).status_code == 403
    assert client.delete(url, headers=h_ana).status_code == 403

    r = client.patch(url, json={"nombre": "Nuevo nombre", "telefono": "611111111"}, headers=h_admin)
    assert r.status_code == 200 and r.json()["nombre"] == "Nuevo nombre"

    client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h_ana)
    client.post(f"{url}/comentarios", json={"mensaje": "Muy rico"}, headers=h_ana)
    imagen_url = client.get(url).json()["imagenes"][0]["url"]

    assert client.delete(url, headers=h_admin).status_code == 204
    assert client.get(url).status_code == 404
    assert client.get(imagen_url).status_code == 404  # el fichero también se borra
    assert client.get("/api/actividades").json() == []


def test_imagenes(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est_id = crear_establecimiento(client, h_ana)["id"]
    url = f"/api/establecimientos/{est_id}"

    r = client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h_ana)
    assert r.status_code == 201
    imagen = r.json()

    from io import BytesIO

    from PIL import Image

    guardada = Image.open(BytesIO(client.get(imagen["url"]).content))
    assert guardada.format == "WEBP" and max(guardada.size) == 1600  # redimensionada

    assert client.get("/api/establecimientos").json()[0]["portada_url"] == imagen["url"]
    assert client.delete(f"{url}/imagenes/{imagen['id']}", headers=h_bea).status_code == 403
    assert client.delete(f"{url}/imagenes/{imagen['id']}", headers=h_ana).status_code == 204
    assert client.get(url).json()["imagenes"] == []


def test_comentarios(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est_id = crear_establecimiento(client, h_ana)["id"]
    url = f"/api/establecimientos/{est_id}/comentarios"

    assert client.post(url, json={"mensaje": "x" * 281}, headers=h_bea).status_code == 422
    assert client.post(url, json={"mensaje": "   "}, headers=h_bea).status_code == 422
    comentario = client.post(url, json={"mensaje": " ¡Todo sin gluten! "}, headers=h_bea).json()
    assert comentario["mensaje"] == "¡Todo sin gluten!"
    assert comentario["autor"]["username"] == "bea"

    assert client.delete(f"/api/comentarios/{comentario['id']}", headers=h_ana).status_code == 403
    assert client.delete(f"/api/comentarios/{comentario['id']}", headers=h_bea).status_code == 204
    assert client.get(url).json() == []


def test_actividades_y_consultas(client):
    h_ana = registrar(client, "ana", 0)
    h_admin = registrar(client, "jefa", 1)
    hacer_admin("jefa")
    est_id = crear_establecimiento(client, h_ana)["id"]
    client.post(f"/api/establecimientos/{est_id}/comentarios", json={"mensaje": "Hola"}, headers=h_ana)

    actividades = client.get("/api/actividades").json()
    assert [a["tipo"] for a in actividades] == ["comentario", "publicacion"]
    assert actividades[0]["establecimiento"]["nombre"] == "Celiaco Feliz"
    # Las fechas salen con zona horaria (si no, el navegador las toma como hora local).
    assert actividades[0]["creado_en"].endswith(("Z", "+00:00"))

    r = client.post("/api/consultas", json={"nombre": "Pepe", "email": "pepe@example.com", "mensaje": "Hola"})
    assert r.status_code == 201
    assert client.get("/api/consultas", headers=h_ana).status_code == 403
    consultas = client.get("/api/consultas", headers=h_admin).json()
    assert len(consultas) == 1
    assert client.delete(f"/api/consultas/{consultas[0]['id']}", headers=h_admin).status_code == 204


def test_chat(client):
    h = registrar(client, "ana")
    token = h["Authorization"].split()[1]

    import pytest
    from starlette.websockets import WebSocketDisconnect

    with pytest.raises(WebSocketDisconnect), client.websocket_connect("/api/chat?token=falso") as ws:
        ws.receive_json()

    with client.websocket_connect(f"/api/chat?token={token}") as ws:
        assert ws.receive_json() == {"tipo": "historial", "mensajes": []}
        ws.send_json({"texto": "¡Hola!"})
        mensaje = ws.receive_json()
        assert mensaje["texto"] == "¡Hola!" and mensaje["autor"]["username"] == "ana"

    # El historial se guarda en la base de datos
    with client.websocket_connect(f"/api/chat?token={token}") as ws:
        historial = ws.receive_json()["mensajes"]
        assert [m["texto"] for m in historial] == ["¡Hola!"]


def test_estadisticas_y_detalle_con_autor(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h)
    crear_establecimiento(client, h, nombre="Otro", cod_postal="18001", provincia="Granada")
    assert client.get("/api/estadisticas").json() == {
        "establecimientos": 2,
        "miembros": 1,
        "opiniones": 0,
        "provincias": 2,
    }
    detalle = client.get(f"/api/establecimientos/{est['id']}").json()
    assert detalle["creado_por"]["username"] == "ana"
