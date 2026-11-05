"""Convierte una dirección en coordenadas con OpenStreetMap (Nominatim).

Se hace una sola vez al crear/editar un establecimiento y se guarda en la base de datos,
en lugar de geocodificar en el navegador cada vez que alguien abre la ficha.
"""

import logging

import httpx

from app.config import get_settings
from app.models import Establecimiento

log = logging.getLogger(__name__)
URL = "https://nominatim.openstreetmap.org/search"


def geocodificar(est: Establecimiento) -> None:
    if not get_settings().geocodificar:
        return
    direccion = ", ".join([est.calle, est.cod_postal, est.localidad, est.provincia, est.pais])
    try:
        respuesta = httpx.get(
            URL,
            params={"q": direccion, "format": "json", "limit": 1},
            headers={"User-Agent": "OcioSinGluten/2.0"},
            timeout=4,
        )
        respuesta.raise_for_status()
        resultados = respuesta.json()
    except (httpx.HTTPError, ValueError) as error:
        log.warning("No se pudo geocodificar '%s': %s", direccion, error)
        return
    if resultados:
        est.latitud = float(resultados[0]["lat"])
        est.longitud = float(resultados[0]["lon"])
