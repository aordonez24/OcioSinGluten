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


def test_like_favorito_y_visita(client):
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
    assert detalle["me_gusta"] and detalle["favorito"] and detalle["visitado"]
    assert client.get(url, headers=h_bea).json()["me_gusta"] is False

    assert [e["id"] for e in client.get("/api/usuarios/ana/favoritos").json()] == [est_id]
    assert len(client.get("/api/usuarios/bea/visitados").json()) == 1

    client.delete(f"{url}/like", headers=h_ana)
    assert client.get(url).json()["num_likes"] == 0
    tipos = [a["tipo"] for a in client.get("/api/actividades?usuario=ana").json()]
    assert "like" not in tipos and "visita" in tipos


def test_permisos_de_edicion_y_borrado(client):
    h_ana = registrar(client, "ana", 0)
    h_admin = registrar(client, "jefa", 1)
    h_otro = registrar(client, "otro", 2)
    hacer_admin("jefa")
    est = crear_establecimiento(client, h_ana)
    est_id = est["id"]
    url = f"/api/establecimientos/{est_id}"
    assert est["puede_editar"] is True
    assert client.get(url, headers=h_otro).json()["puede_editar"] is False

    # Quien lo añadió puede corregirlo; el resto no. Borrar solo puede el equipo de administración.
    assert client.patch(url, json={"nombre": "Nuevo"}, headers=h_otro).status_code == 403
    assert client.patch(url, json={"nombre": "Nuevo"}, headers=h_ana).status_code == 200
    assert client.delete(url, headers=h_ana).status_code == 403

    r = client.patch(url, json={"nombre": "Nuevo nombre", "telefono": "611111111"}, headers=h_admin)
    assert r.status_code == 200 and r.json()["nombre"] == "Nuevo nombre"

    client.post(f"{url}/imagenes", files={"archivo": ("a.png", imagen_png(), "image/png")}, headers=h_ana)
    client.post(f"{url}/comentarios", json={"puntuacion": 5, "mensaje": "Muy rico"}, headers=h_ana)
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


def test_opiniones_con_puntuacion(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    h_carla = registrar(client, "carla", 2)
    est_id = crear_establecimiento(client, h_ana)["id"]
    url = f"/api/establecimientos/{est_id}/comentarios"

    for malo in (
        {"puntuacion": 5, "mensaje": "x" * 501},
        {"puntuacion": 5, "mensaje": "   "},
        {"puntuacion": 0, "mensaje": "Cero estrellas no existe"},
        {"puntuacion": 6, "mensaje": "Demasiadas"},
        {"mensaje": "Sin puntuación"},
    ):
        assert client.post(url, json=malo, headers=h_bea).status_code == 422, malo

    opinion = client.post(url, json={"puntuacion": 4, "mensaje": " ¡Todo sin gluten! "}, headers=h_bea).json()
    assert opinion["mensaje"] == "¡Todo sin gluten!" and opinion["puntuacion"] == 4
    assert opinion["autor"]["username"] == "bea" and opinion["editado_en"] is None

    # Una opinión por persona y sitio
    otra = client.post(url, json={"puntuacion": 1, "mensaje": "Otra vez"}, headers=h_bea)
    assert otra.status_code == 409

    client.post(url, json={"puntuacion": 5, "mensaje": "Genial"}, headers=h_carla)
    est = client.get(f"/api/establecimientos/{est_id}").json()
    assert (est["valoracion"], est["num_opiniones"]) == (4.5, 2)

    # Editar: solo la autora (ni siquiera un admin puede cambiar lo que opina otra persona)
    editar = f"/api/comentarios/{opinion['id']}"
    assert client.patch(editar, json={"puntuacion": 1}, headers=h_ana).status_code == 403
    editada = client.patch(editar, json={"puntuacion": 2}, headers=h_bea).json()
    assert editada["puntuacion"] == 2 and editada["mensaje"] == "¡Todo sin gluten!"
    assert editada["editado_en"] is not None
    assert client.get(f"/api/establecimientos/{est_id}").json()["valoracion"] == 3.5

    # Borrar: autora o admin; también desaparece del feed de actividad
    assert client.delete(editar, headers=h_ana).status_code == 403
    assert client.delete(editar, headers=h_bea).status_code == 204
    assert [c["autor"]["username"] for c in client.get(url).json()] == ["carla"]
    assert "comentario" not in [a["tipo"] for a in client.get("/api/actividades?usuario=bea").json()]


def test_categoria_y_distintivos(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h, categoria="panaderia", todo_sin_gluten=True, cocina_separada=True)
    assert est["categoria"] == "panaderia"
    assert (est["todo_sin_gluten"], est["cocina_separada"], est["carta_sin_gluten"]) == (True, True, False)
    assert (
        client.post(
            "/api/establecimientos",
            json={
                "nombre": "X",
                "telefono": "953000000",
                "calle": "C",
                "cod_postal": "23001",
                "localidad": "Jaén",
                "provincia": "Jaén",
                "categoria": "discoteca",
            },
            headers=h,
        ).status_code
        == 422
    )

    listado = client.get("/api/establecimientos").json()[0]
    assert listado["categoria"] == "panaderia" and listado["todo_sin_gluten"] is True


def test_actividades_y_consultas(client):
    h_ana = registrar(client, "ana", 0)
    h_admin = registrar(client, "jefa", 1)
    hacer_admin("jefa")
    est_id = crear_establecimiento(client, h_ana)["id"]
    client.post(
        f"/api/establecimientos/{est_id}/comentarios",
        json={"puntuacion": 5, "mensaje": "Hola"},
        headers=h_ana,
    )

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


def test_informacion_practica(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h, precio=2, horario="De lunes a viernes", web="celiacofeliz.es")
    assert (est["precio"], est["horario"], est["web"]) == (2, "De lunes a viernes", "https://celiacofeliz.es")
    url = f"/api/establecimientos/{est['id']}"

    assert client.patch(url, json={"web": "no es una web"}, headers=h).status_code == 422
    assert client.patch(url, json={"precio": 4}, headers=h).status_code == 422
    # Cadena vacía y precio 0 borran el dato
    r = client.patch(url, json={"web": "", "horario": "", "precio": 0}, headers=h).json()
    assert (r["precio"], r["horario"], r["web"]) == (None, None, None)
    assert client.get("/api/establecimientos").json()[0]["precio"] is None


def test_avisos_de_un_sitio_llegan_a_administracion(client):
    h_ana = registrar(client, "ana", 0)
    h_admin = registrar(client, "jefa", 1)
    hacer_admin("jefa")
    est = crear_establecimiento(client, h_admin)
    url = f"/api/establecimientos/{est['id']}/avisos"

    assert client.post(url, json={"motivo": "cerrado"}).status_code == 401
    assert client.post(url, json={"motivo": "inventado"}, headers=h_ana).status_code == 422
    r = client.post(url, json={"motivo": "cerrado", "mensaje": "Han cerrado en verano"}, headers=h_ana)
    assert r.status_code == 201

    [aviso] = client.get("/api/consultas", headers=h_admin).json()
    assert aviso["motivo"] == "cerrado"
    assert aviso["nombre"] == "ana" and aviso["email"] == "ana@example.com"
    assert aviso["establecimiento"]["nombre"] == "Celiaco Feliz"


def test_opiniones_en_el_perfil(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h)
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios",
        json={"puntuacion": 4, "mensaje": "Muy bien"},
        headers=h,
    )
    [opinion] = client.get("/api/usuarios/ana/opiniones").json()
    assert opinion["puntuacion"] == 4
    assert opinion["establecimiento"]["nombre"] == "Celiaco Feliz"
