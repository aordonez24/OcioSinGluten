from conftest import crear_establecimiento, hacer_admin, imagen_png, registrar


def test_registro_y_login(client):
    registrar(client, "ana")
    r = client.post("/api/auth/login", json={"email": "ANA@example.com", "password": "Secreta123"})
    assert r.status_code == 200
    usuario = r.json()["usuario"]
    assert usuario["username"] == "ana"
    assert "password" not in usuario and "password_hash" not in usuario


def test_login_incorrecto_no_revela_si_existe_la_cuenta(client):
    registrar(client, "ana")
    malo = client.post("/api/auth/login", json={"email": "ana@example.com", "password": "Otra1234"})
    inexistente = client.post("/api/auth/login", json={"email": "x@example.com", "password": "Otra1234"})
    assert malo.status_code == inexistente.status_code == 401
    assert malo.json() == inexistente.json()


def test_registro_valida_datos(client):
    base = {
        "dni": "12345678Z",
        "username": "ana",
        "nombre": "Ana",
        "apellidos": "P",
        "fecha_nacimiento": "1990-01-01",
        "telefono": "600123123",
        "email": "ana@example.com",
        "password": "Secreta123",
    }
    for campo, valor in [
        ("dni", "12345678A"),  # letra de control incorrecta
        ("telefono", "12345"),
        ("password", "corta"),
        ("password", "sinmayusculas1"),
        ("fecha_nacimiento", "2020-01-01"),  # menor de edad
        ("email", "no-es-un-email"),
    ]:
        r = client.post("/api/auth/registro", json={**base, campo: valor})
        assert r.status_code == 422, (campo, valor)


def test_registro_duplicado(client):
    registrar(client, "ana")
    r = client.post(
        "/api/auth/registro",
        json={
            "dni": "87654321X",
            "username": "ana",
            "nombre": "Ana",
            "apellidos": "P",
            "fecha_nacimiento": "1990-01-01",
            "telefono": "600123123",
            "email": "otra@example.com",
            "password": "Secreta123",
        },
    )
    assert r.status_code == 409
    assert "nombre de usuario" in r.json()["detail"]


def test_email_admin_no_da_permisos(client):
    """En la versión anterior cualquier email con 'admin' en el dominio era administrador."""
    h = registrar(client, "pirata", email="pirata@admin.com")
    assert client.get("/api/auth/yo", headers=h).json()["rol"] == "comun"
    assert client.get("/api/consultas", headers=h).status_code == 403


def test_endpoints_protegidos_requieren_sesion(client):
    assert client.patch("/api/usuarios/yo", json={"nombre": "X"}).status_code == 401
    assert client.post("/api/establecimientos", json={}).status_code == 401
    assert client.delete("/api/usuarios/alguien").status_code == 401


def test_perfil_oculta_datos_privados_a_otros(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    assert client.get("/api/usuarios/ana", headers=h_bea).json()["privado"] is None
    assert client.get("/api/usuarios/ana").json()["privado"] is None
    assert client.get("/api/usuarios/ana", headers=h_ana).json()["privado"]["dni"] == "12345678Z"


def test_editar_perfil_y_password(client):
    h = registrar(client, "ana")
    r = client.patch("/api/usuarios/yo", json={"nombre": "Anita", "telefono": "611 222 333"}, headers=h)
    assert r.status_code == 200
    assert r.json()["nombre"] == "Anita" and r.json()["telefono"] == "611222333"

    mal = client.put(
        "/api/usuarios/yo/password", json={"actual": "Mala1234", "nueva": "Nueva1234"}, headers=h
    )
    assert mal.status_code == 400
    ok = client.put(
        "/api/usuarios/yo/password", json={"actual": "Secreta123", "nueva": "Nueva1234"}, headers=h
    )
    assert ok.status_code == 200
    # La sesión anterior se cierra; la nueva (la que devuelve el cambio) sigue abierta
    assert client.get("/api/auth/yo", headers=h).status_code == 401
    nueva = {"Authorization": f"Bearer {ok.json()['access_token']}"}
    assert client.get("/api/auth/yo", headers=nueva).status_code == 200
    assert (
        client.post("/api/auth/login", json={"email": "ana@example.com", "password": "Nueva1234"}).status_code
        == 200
    )


def test_seguir_y_dejar_de_seguir(client):
    h_ana = registrar(client, "ana", 0)
    registrar(client, "bea", 1)
    assert client.put("/api/usuarios/bea/seguir", headers=h_ana).status_code == 204
    assert client.put("/api/usuarios/bea/seguir", headers=h_ana).status_code == 204  # idempotente
    assert client.put("/api/usuarios/ana/seguir", headers=h_ana).status_code == 400

    perfil = client.get("/api/usuarios/bea", headers=h_ana).json()
    assert perfil["num_seguidores"] == 1 and perfil["siguiendo"] is True
    assert [u["username"] for u in client.get("/api/usuarios/ana/seguidos").json()] == ["bea"]

    client.delete("/api/usuarios/bea/seguir", headers=h_ana)
    assert client.get("/api/usuarios/bea").json()["num_seguidores"] == 0


def test_foto_de_perfil(client):
    h = registrar(client, "ana")
    r = client.put(
        "/api/usuarios/yo/foto", files={"archivo": ("f.png", imagen_png(), "image/png")}, headers=h
    )
    assert r.status_code == 200
    url = r.json()["foto_url"]
    assert url.endswith(".webp")
    assert client.get(url).status_code == 200

    falsa = client.put(
        "/api/usuarios/yo/foto", files={"archivo": ("f.png", b"no soy una imagen", "image/png")}, headers=h
    )
    assert falsa.status_code == 422

    assert client.delete("/api/usuarios/yo/foto", headers=h).json()["foto_url"] is None
    assert client.get(url).status_code == 404


def test_suspender_usuario(client):
    h_admin = registrar(client, "admin", 0)
    h_ana = registrar(client, "ana", 1)
    hacer_admin("admin")

    assert (
        client.patch("/api/usuarios/admin/archivado", json={"archivado": True}, headers=h_ana).status_code
        == 403
    )
    assert (
        client.patch("/api/usuarios/ana/archivado", json={"archivado": True}, headers=h_admin).status_code
        == 200
    )

    assert client.get("/api/auth/yo", headers=h_ana).status_code == 401  # su token deja de valer
    assert (
        client.post(
            "/api/auth/login", json={"email": "ana@example.com", "password": "Secreta123"}
        ).status_code
        == 403
    )
    assert client.get("/api/usuarios/ana").status_code == 404
    # El admin ve también las cuentas suspendidas (y nunca se lista a sí mismo)
    assert [u["username"] for u in client.get("/api/usuarios", headers=h_admin).json()] == ["ana"]


def test_eliminar_usuario_borra_en_cascada(client):
    from conftest import crear_establecimiento

    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est = crear_establecimiento(client, h_ana)
    client.put(f"/api/establecimientos/{est['id']}/like", headers=h_bea)
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios",
        json={"puntuacion": 5, "mensaje": "Genial"},
        headers=h_bea,
    )
    client.put("/api/usuarios/ana/seguir", headers=h_bea)

    assert client.delete("/api/usuarios/bea", headers=h_ana).status_code == 403
    assert client.delete("/api/usuarios/bea", headers=h_bea).status_code == 204

    detalle = client.get(f"/api/establecimientos/{est['id']}").json()
    assert detalle["num_likes"] == 0
    assert client.get(f"/api/establecimientos/{est['id']}/comentarios").json() == []
    assert client.get("/api/usuarios/ana").json()["num_seguidores"] == 0


def test_login_limita_intentos_fallidos(client):
    registrar(client, "ana")
    malo = {"email": "ana@example.com", "password": "Mala12345"}
    for _ in range(5):
        assert client.post("/api/auth/login", json=malo).status_code == 401
    assert client.post("/api/auth/login", json=malo).status_code == 429
    # Bloqueado incluso con la contraseña buena hasta que pase la ventana.
    bueno = {"email": "ana@example.com", "password": "Secreta123"}
    assert client.post("/api/auth/login", json=bueno).status_code == 429


def test_comunidad_y_contadores_de_perfil(client):
    from conftest import crear_establecimiento

    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    registrar(client, "carla", 2)
    client.put("/api/usuarios/carla/seguir", headers=h_ana)
    client.put("/api/usuarios/carla/seguir", headers=h_bea)

    comunidad = client.get("/api/usuarios", headers=h_ana).json()
    assert [(u["username"], u["num_seguidores"], u["siguiendo"]) for u in comunidad] == [
        ("carla", 2, True),
        ("bea", 0, False),
    ]

    est = crear_establecimiento(client, h_ana)
    client.put(f"/api/establecimientos/{est['id']}/visita", headers=h_ana)
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios",
        json={"puntuacion": 4, "mensaje": "Top"},
        headers=h_ana,
    )
    perfil = client.get("/api/usuarios/ana").json()
    assert (perfil["num_opiniones"], perfil["num_visitados"], perfil["num_seguidos"]) == (1, 1, 1)


def test_cabeceras_de_seguridad(client):
    r = client.get("/api/estadisticas")
    assert r.headers["x-content-type-options"] == "nosniff"
    assert r.headers["x-frame-options"] == "DENY"
    csp = r.headers["content-security-policy"]
    assert "script-src 'self'" in csp and "frame-ancestors 'none'" in csp
    # La documentación interactiva (Swagger) usa un CDN: no se le aplica la CSP
    assert "content-security-policy" not in client.get("/docs").headers


def test_actividad_de_quienes_sigo(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    h_carlos = registrar(client, "carlos", 2)
    crear_establecimiento(client, h_bea)
    crear_establecimiento(client, h_carlos, nombre="Otro sitio")

    assert client.get("/api/actividades?siguiendo=true").status_code == 401
    assert client.get("/api/actividades?siguiendo=true", headers=h_ana).json() == []
    client.put("/api/usuarios/bea/seguir", headers=h_ana)
    feed = client.get("/api/actividades?siguiendo=true", headers=h_ana).json()
    assert [a["usuario"]["username"] for a in feed] == ["bea"]
    assert len(client.get("/api/actividades").json()) == 2


def test_notificaciones(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est = crear_establecimiento(client, h_ana)

    assert client.get("/api/notificaciones").status_code == 401
    assert client.get("/api/notificaciones", headers=h_ana).json() == {"no_leidas": 0, "notificaciones": []}

    # Bea sigue a Ana (dos veces: no se repite) y opina sobre el sitio que Ana añadió
    client.put("/api/usuarios/ana/seguir", headers=h_bea)
    client.delete("/api/usuarios/ana/seguir", headers=h_bea)
    client.put("/api/usuarios/ana/seguir", headers=h_bea)
    r = client.post(
        f"/api/establecimientos/{est['id']}/comentarios",
        json={"puntuacion": 5, "mensaje": "Genial"},
        headers=h_bea,
    )
    opinion_id = r.json()["id"]

    datos = client.get("/api/notificaciones", headers=h_ana).json()
    assert datos["no_leidas"] == 2
    assert [n["tipo"] for n in datos["notificaciones"]] == ["opinion", "seguidor"]
    assert datos["notificaciones"][0]["actor"]["username"] == "bea"
    assert datos["notificaciones"][0]["establecimiento"]["nombre"] == "Celiaco Feliz"

    assert client.post("/api/notificaciones/leidas", headers=h_ana).status_code == 204
    assert client.get("/api/notificaciones", headers=h_ana).json()["no_leidas"] == 0

    # A Ana le resulta útil la opinión de Bea: le llega a Bea
    assert client.put(f"/api/comentarios/{opinion_id}/util", headers=h_ana).status_code == 204
    [nota] = client.get("/api/notificaciones", headers=h_bea).json()["notificaciones"]
    assert nota["tipo"] == "util"


def test_opiniones_utiles(client):
    h_ana = registrar(client, "ana", 0)
    h_bea = registrar(client, "bea", 1)
    est = crear_establecimiento(client, h_ana)
    url = f"/api/establecimientos/{est['id']}/comentarios"
    opinion_id = client.post(url, json={"puntuacion": 4, "mensaje": "Bien"}, headers=h_ana).json()["id"]

    assert client.put(f"/api/comentarios/{opinion_id}/util", headers=h_ana).status_code == 400  # la propia
    assert client.put(f"/api/comentarios/{opinion_id}/util").status_code == 401
    client.put(f"/api/comentarios/{opinion_id}/util", headers=h_bea)
    client.put(f"/api/comentarios/{opinion_id}/util", headers=h_bea)  # idempotente

    [vista_bea] = client.get(url, headers=h_bea).json()
    assert (vista_bea["num_utiles"], vista_bea["util"]) == (1, True)
    [anonima] = client.get(url).json()
    assert (anonima["num_utiles"], anonima["util"]) == (1, False)

    client.delete(f"/api/comentarios/{opinion_id}/util", headers=h_bea)
    assert client.get(url).json()[0]["num_utiles"] == 0


def test_descargar_mis_datos(client):
    h = registrar(client, "ana")
    otra = registrar(client, "bea", n=1)
    est = crear_establecimiento(client, h)
    client.post(
        f"/api/establecimientos/{est['id']}/comentarios", json={"puntuacion": 4, "mensaje": "Rico"}, headers=h
    )
    client.put(f"/api/establecimientos/{est['id']}/favorito", headers=h)
    client.put("/api/usuarios/ana/seguir", headers=otra)

    r = client.get("/api/usuarios/yo/datos", headers=h)
    assert r.status_code == 200
    assert r.headers["content-disposition"] == 'attachment; filename="ociosingluten-ana.json"'
    assert r.headers["cache-control"] == "no-store"
    datos = r.json()
    assert datos["cuenta"]["email"] == "ana@example.com" and datos["cuenta"]["dni"] == "12345678Z"
    assert "password_hash" not in str(datos)
    assert datos["opiniones"][0]["mensaje"] == "Rico"
    assert datos["establecimientos_anadidos"][0]["nombre"] == "Celiaco Feliz"
    assert [f["nombre"] for f in datos["favoritos"]] == ["Celiaco Feliz"]
    assert [p["username"] for p in datos["te_siguen"]] == ["bea"]
    assert datos["sigues_a"] == []

    # Solo los datos de quien lo pide
    assert client.get("/api/usuarios/yo/datos", headers=otra).json()["opiniones"] == []
    assert client.get("/api/usuarios/yo/datos").status_code == 401


def test_la_app_movil_puede_llamar_a_la_api(client):
    """La app nativa carga el frontend desde https://localhost (Android) o capacitor://localhost (iOS)."""
    for origen in ("https://localhost", "capacitor://localhost"):
        r = client.options(
            "/api/auth/login",
            headers={
                "Origin": origen,
                "Access-Control-Request-Method": "POST",
                "Access-Control-Request-Headers": "content-type,authorization",
            },
        )
        assert r.status_code == 200 and r.headers["access-control-allow-origin"] == origen
    otra_web = client.options(
        "/api/auth/login", headers={"Origin": "https://otra-web.com", "Access-Control-Request-Method": "POST"}
    )
    assert "access-control-allow-origin" not in otra_web.headers


def test_insignias(client):
    ana = registrar(client, "ana")
    bea = registrar(client, "bea", n=1)
    sitios = [
        crear_establecimiento(client, ana, nombre=f"Sitio {p}", provincia=p, cod_postal=f"2300{i}")
        for i, p in enumerate(["Jaén", "Granada", "Málaga"])
    ]
    opinion = client.post(
        f"/api/establecimientos/{sitios[0]['id']}/comentarios",
        json={"puntuacion": 5, "mensaje": "Genial"},
        headers=ana,
    ).json()
    client.put(f"/api/comentarios/{opinion['id']}/util", headers=bea)
    for s in sitios[:2]:
        client.put(f"/api/establecimientos/{s['id']}/visita", headers=ana)

    propias = {i["clave"]: i for i in client.get("/api/usuarios/ana", headers=ana).json()["insignias"]}
    assert propias["primera_opinion"]["conseguida"] and propias["abre_camino"]["conseguida"]
    assert propias["trotamundos"] == {**propias["trotamundos"], "actual": 2, "meta": 3, "conseguida": False}
    assert propias["ayuda"]["actual"] == 1
    assert list(propias)[:2] == ["primera_opinion", "abre_camino"]  # primero las conseguidas

    # Quien visita el perfil solo ve las conseguidas
    ajenas = client.get("/api/usuarios/ana", headers=bea).json()["insignias"]
    assert {i["clave"] for i in ajenas} == {"primera_opinion", "abre_camino"}
    assert client.get("/api/usuarios/ana").json()["insignias"] == ajenas

    client.put(f"/api/establecimientos/{sitios[2]['id']}/visita", headers=ana)
    ajenas = {i["clave"] for i in client.get("/api/usuarios/ana").json()["insignias"]}
    assert "trotamundos" in ajenas
