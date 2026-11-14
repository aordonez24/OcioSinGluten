"""Importación de sitios reales de OpenStreetMap."""

import pytest

from app.db import SessionLocal
from app.services import osm
from conftest import crear_establecimiento, registrar

MAÑANAS = [{"abre": "09:00", "cierra": "14:00"}]


@pytest.mark.parametrize(
    ("texto", "esperado"),
    [
        ("Mo-Fr 09:00-14:00; Sa,Su off", [MAÑANAS] * 5 + [[], []]),
        ("Mo-Fr 09:00-14:00; PH off", [MAÑANAS] * 5 + [[], []]),  # los festivos se ignoran
        ("Mo, We 09:00-14:00", [MAÑANAS, [], MAÑANAS, [], [], [], []]),
        ("Mo-Su 09:00-14:00; Su off", [MAÑANAS] * 6 + [[]]),  # la regla posterior manda
        ("Fr-Mo 09:00-14:00", [MAÑANAS, [], [], [], MAÑANAS, MAÑANAS, MAÑANAS]),  # rango que da la vuelta
        ("09:00-14:00", [MAÑANAS] * 7),
        ("24/7", [[{"abre": "00:00", "cierra": "23:59"}]] * 7),
        ("Mo-Su 20:00-24:00", [[{"abre": "20:00", "cierra": "00:00"}]] * 7),
        ("Tu-Su 13:00-01:00", [[]] + [[{"abre": "13:00", "cierra": "01:00"}]] * 6),
        # Lo que no se entiende no se adivina
        ("Jun-Sep: Mo-Su 10:00-23:00", None),
        ("Mo-Fr sunrise-sunset", None),
        ("Mo 09:00-14:00,12:00-16:00", None),  # franjas solapadas
        ("", None),
    ],
)
def test_horario_de_openstreetmap(texto, esperado):
    assert osm.horario(texto) == esperado


def test_telefono_web_y_provincia():
    assert osm.telefono({"phone": "+34 953 12 34 56"}) == "953123456"
    assert osm.telefono({"contact:phone": "0034 600-111-222; +34 953 000 000"}) == "600111222"
    assert osm.telefono({"phone": "112"}) is None
    assert osm.web({"website": "ejemplo.es"}) == "https://ejemplo.es"
    assert osm.web({"website": "no es una web"}) is None
    assert osm.PROVINCIAS["23"] == "Jaén" and osm.PROVINCIAS["38"] == "Santa Cruz de Tenerife"
    assert len(osm.PROVINCIAS) == 52


def _elemento(id_, **etiquetas):
    return {
        "type": "node",
        "id": id_,
        "lat": 37.77,
        "lon": -3.79,
        "tags": {
            "name": "Celiaco Feliz",
            "amenity": "restaurant",
            "diet:gluten_free": "yes",
            "addr:street": "Calle Maestra",
            "addr:housenumber": "10",
            "addr:postcode": "23002",
            "addr:city": "Jaén",
            **etiquetas,
        },
    }


def test_convertir_un_sitio():
    datos = osm.convertir(_elemento(1, **{"diet:gluten_free": "only", "opening_hours": "Mo-Fr 09:00-14:00"}))
    assert datos["osm_id"] == "node/1"
    assert datos["calle"] == "Calle Maestra, 10" and datos["provincia"] == "Jaén"
    assert datos["todo_sin_gluten"] is True and datos["telefono"] is None
    assert datos["horario"][0] == MAÑANAS

    assert osm.convertir(_elemento(2, amenity="pharmacy")) is None  # no es un sitio de ocio
    assert osm.convertir(_elemento(3, **{"addr:postcode": "99999"})) is None  # código postal imposible

    # Sin dirección en OpenStreetMap: se completa con la de Nominatim
    sin_direccion = _elemento(4)
    for clave in ("addr:street", "addr:housenumber", "addr:postcode", "addr:city"):
        del sin_direccion["tags"][clave]
    assert osm.convertir(sin_direccion) is None
    nominatim = {"road": "Calle Real", "house_number": "2", "postcode": "28013", "city": "Madrid"}
    datos = osm.convertir(sin_direccion, nominatim)
    assert datos["calle"] == "Calle Real, 2" and datos["provincia"] == "Madrid"


def test_importar_no_duplica(client):
    h = registrar(client, "ana")
    crear_establecimiento(client, h)  # "Celiaco Feliz" en el 23002, añadido a mano
    elementos = [
        _elemento(1),  # el mismo sitio que ya estaba
        _elemento(2, name="Horno Sin Trigo"),
        _elemento(3, name="Sin dirección", **{"addr:postcode": ""}),
    ]
    with SessionLocal() as db:
        assert osm.importar(db, elementos) == {"nuevos": 1, "ya_estaban": 1, "incompletos": 1}
        assert osm.importar(db, elementos) == {"nuevos": 0, "ya_estaban": 2, "incompletos": 1}

    sitios = {e["nombre"]: e for e in client.get("/api/establecimientos").json()}
    importado = client.get(f"/api/establecimientos/{sitios['Horno Sin Trigo']['id']}").json()
    assert importado["osm_url"] == "https://www.openstreetmap.org/node/2"
    assert importado["telefono"] is None and importado["creado_por"] is None
    assert client.get(f"/api/establecimientos/{sitios['Celiaco Feliz']['id']}").json()["osm_url"] is None


def test_el_telefono_es_opcional(client):
    h = registrar(client, "ana")
    est = crear_establecimiento(client, h, telefono=None)
    assert est["telefono"] is None
    url = f"/api/establecimientos/{est['id']}"
    assert client.patch(url, json={"telefono": "953 11 22 33"}, headers=h).json()["telefono"] == "953112233"
    assert client.patch(url, json={"telefono": ""}, headers=h).json()["telefono"] is None
