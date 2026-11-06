from app import cli


def test_demo_carga_datos_coherentes(client):
    cli.cargar_demo()
    stats = client.get("/api/estadisticas").json()
    assert stats["establecimientos"] == len(cli.ESTABLECIMIENTOS)
    assert stats["miembros"] == len(cli.USUARIOS)

    # Cada sitio tiene fotos con su autoría y licencia, y los ficheros existen
    for est in client.get("/api/establecimientos").json():
        assert est["portada_url"]
        assert client.get(est["portada_url"]).status_code == 200
        detalle = client.get(f"/api/establecimientos/{est['id']}").json()
        assert all("Wikimedia Commons" in img["credito"] for img in detalle["imagenes"])
        assert all(
            img["credito_url"].startswith("https://commons.wikimedia.org/") for img in detalle["imagenes"]
        )

    r = client.post("/api/auth/login", json={"email": "lucia@example.com", "password": "Lucia1234"})
    assert r.status_code == 200

    # El feed sale en orden cronológico (más reciente primero)
    fechas = [a["creado_en"] for a in client.get("/api/actividades?limite=200").json()]
    assert fechas == sorted(fechas, reverse=True)


def test_hacer_admin(client):
    from conftest import registrar

    h = registrar(client, "ana")
    cli.hacer_admin("ANA@example.com")
    assert client.get("/api/auth/yo", headers=h).json()["rol"] == "admin"
