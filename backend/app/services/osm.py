"""Importa de OpenStreetMap los sitios de España marcados como sin gluten (diet:gluten_free=yes/only).

Así la aplicación arranca con sitios reales en lugar de vacía. Los datos de OpenStreetMap son de sus
colaboradores (licencia ODbL): cada sitio guarda su `osm_id` y su ficha enlaza a OpenStreetMap.

A los sitios sin código postal o sin calle se les busca la dirección por sus coordenadas con Nominatim,
respetando su límite de una petición por segundo y guardando las respuestas en una caché.
"""

import json
import logging
import re
import time
from collections.abc import Iterable
from pathlib import Path

import httpx
from pydantic import TypeAdapter, ValidationError
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.models import Categoria, Establecimiento
from app.schemas import Horario, Web

log = logging.getLogger(__name__)
AGENTE = {"User-Agent": "OcioSinGluten/2.0 (https://ociosingluten.onrender.com)"}
OVERPASS = (
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
)
CONSULTA = """[out:json][timeout:180];
area["ISO3166-1"="ES"][admin_level=2]->.es;
nwr["diet:gluten_free"~"^(yes|only)$"](area.es);
out tags center;"""

CATEGORIAS = {
    "restaurant": Categoria.RESTAURANTE,
    "fast_food": Categoria.RESTAURANTE,
    "cafe": Categoria.CAFETERIA,
    "bakery": Categoria.PANADERIA,
    "pastry": Categoria.PANADERIA,
    "confectionery": Categoria.PANADERIA,
    "ice_cream": Categoria.HELADERIA,
    "bar": Categoria.BAR,
    "pub": Categoria.BAR,
    "biergarten": Categoria.BAR,
}

# Las dos primeras cifras del código postal son las de la provincia (los mismos nombres que usa el
# frontend en src/provincias.js).
PROVINCIAS = {
    "01": "Álava",
    "02": "Albacete",
    "03": "Alicante",
    "04": "Almería",
    "05": "Ávila",
    "06": "Badajoz",
    "07": "Illes Balears",
    "08": "Barcelona",
    "09": "Burgos",
    "10": "Cáceres",
    "11": "Cádiz",
    "12": "Castellón",
    "13": "Ciudad Real",
    "14": "Córdoba",
    "15": "A Coruña",
    "16": "Cuenca",
    "17": "Girona",
    "18": "Granada",
    "19": "Guadalajara",
    "20": "Gipuzkoa",
    "21": "Huelva",
    "22": "Huesca",
    "23": "Jaén",
    "24": "León",
    "25": "Lleida",
    "26": "La Rioja",
    "27": "Lugo",
    "28": "Madrid",
    "29": "Málaga",
    "30": "Murcia",
    "31": "Navarra",
    "32": "Ourense",
    "33": "Asturias",
    "34": "Palencia",
    "35": "Las Palmas",
    "36": "Pontevedra",
    "37": "Salamanca",
    "38": "Santa Cruz de Tenerife",
    "39": "Cantabria",
    "40": "Segovia",
    "41": "Sevilla",
    "42": "Soria",
    "43": "Tarragona",
    "44": "Teruel",
    "45": "Toledo",
    "46": "Valencia",
    "47": "Valladolid",
    "48": "Bizkaia",
    "49": "Zamora",
    "50": "Zaragoza",
    "51": "Ceuta",
    "52": "Melilla",
}


# ----------------------------------------------------------------- horario (opening_hours)

DIAS = ["Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"]
# Festivos y vacaciones escolares: la aplicación no los tiene en cuenta, así que sus reglas se ignoran.
FESTIVOS = ("PH", "SH")
_HORARIO = TypeAdapter(Horario)
_TRAMO = re.compile(r"(\d{1,2}):(\d{2})-(\d{1,2}):(\d{2})")


def _dias(texto: str) -> list[int] | None:
    dias: list[int] = []
    for parte in texto.split(","):
        if parte in FESTIVOS:
            continue
        if "-" in parte:
            inicio, _, fin = parte.partition("-")
            if inicio not in DIAS or fin not in DIAS:
                return None
            i, f = DIAS.index(inicio), DIAS.index(fin)
            dias += list(range(i, f + 1)) if i <= f else [*range(i, 7), *range(0, f + 1)]
        elif parte in DIAS:
            dias.append(DIAS.index(parte))
        else:
            return None
    return dias


def _hora(h: str, m: str) -> str:
    return "00:00" if (int(h), int(m)) >= (24, 0) else f"{int(h):02d}:{m}"


def _franjas(texto: str) -> list[dict] | None:
    franjas = []
    for tramo in texto.split(","):
        coincidencia = _TRAMO.fullmatch(tramo.strip())
        if not coincidencia:
            return None
        h1, m1, h2, m2 = coincidencia.groups()
        abre, cierra = _hora(h1, m1), _hora(h2, m2)
        if abre == cierra:  # 00:00-24:00: todo el día
            cierra = "23:59"
        franjas.append({"abre": abre, "cierra": cierra})
    return franjas


def horario(opening_hours: str | None) -> list[list[dict]] | None:
    """Convierte las formas habituales de opening_hours ("Mo-Fr 09:00-14:00; Sa off", "24/7") en el
    horario semanal de la aplicación. Si hay algo que no entiende (festivos, meses, "sunset"…) devuelve
    None: es mejor no mostrar horario que mostrar uno equivocado."""
    if not opening_hours:
        return None
    texto = opening_hours.strip()
    if texto == "24/7":
        return [[{"abre": "00:00", "cierra": "23:59"}] for _ in DIAS]
    semana: list[list[dict]] = [[] for _ in DIAS]
    for regla in filter(None, (r.strip() for r in texto.split(";"))):
        regla = re.sub(r"(?<=[A-Za-z]),\s+", ",", regla)  # "Mo, Su 13:00-16:00" → "Mo,Su 13:00-16:00"
        dias_texto, _, resto = regla.partition(" ")
        if dias_texto in FESTIVOS:
            continue
        if _TRAMO.match(dias_texto):  # sin días: se aplica a toda la semana
            dias, resto = list(range(7)), regla
        else:
            dias = _dias(dias_texto)
        if dias is None:
            return None
        resto = resto.replace(" ", "")
        franjas = [] if resto in ("off", "closed") else _franjas(resto)
        if franjas is None:
            return None
        for dia in dias:  # como en OpenStreetMap, una regla posterior sustituye a la anterior
            semana[dia] = franjas
    try:
        return [[f.model_dump() for f in dia] for dia in _HORARIO.validate_python(semana)]
    except ValidationError:
        return None


# ----------------------------------------------------------------- resto de datos


def telefono(etiquetas: dict) -> str | None:
    crudo = (etiquetas.get("phone") or etiquetas.get("contact:phone") or "").split(";")[0]
    numero = re.sub(r"[^\d+]", "", crudo).removeprefix("+34").removeprefix("0034")
    return numero if re.fullmatch(r"[6789]\d{8}", numero) else None


_WEB = TypeAdapter(Web)


def web(etiquetas: dict) -> str | None:
    crudo = (etiquetas.get("website") or etiquetas.get("contact:website") or "").split(";")[0].strip()
    try:
        return _WEB.validate_python(crudo) if crudo and len(crudo) <= 200 else None
    except ValidationError:
        return None


def categoria(etiquetas: dict) -> Categoria | None:
    return CATEGORIAS.get(etiquetas.get("amenity") or etiquetas.get("shop") or "")


def osm_id(elemento: dict) -> str:
    return f"{elemento['type']}/{elemento['id']}"


def coordenadas(elemento: dict) -> tuple[float, float] | None:
    punto = elemento.get("center") or elemento
    if "lat" in punto and "lon" in punto:
        return punto["lat"], punto["lon"]
    return None


def convertir(elemento: dict, direccion: dict | None = None) -> dict | None:
    """Datos de un Establecimiento a partir de un elemento de OpenStreetMap, o None si falta algo
    imprescindible (nombre, tipo de sitio, coordenadas o una dirección completa)."""
    etiquetas = elemento.get("tags", {})
    tipo, punto, nombre = categoria(etiquetas), coordenadas(elemento), etiquetas.get("name", "").strip()
    if not (tipo and punto and nombre):
        return None
    direccion = direccion or {}
    calle = etiquetas.get("addr:street") or direccion.get("road")
    numero = etiquetas.get("addr:housenumber") or (direccion.get("house_number") if calle else None)
    cod_postal = (etiquetas.get("addr:postcode") or direccion.get("postcode") or "").strip()
    localidad = (
        etiquetas.get("addr:city")
        or direccion.get("city")
        or direccion.get("town")
        or direccion.get("village")
        or direccion.get("municipality")
    )
    if not (calle and localidad and re.fullmatch(r"\d{5}", cod_postal) and cod_postal[:2] in PROVINCIAS):
        return None
    return {
        "osm_id": osm_id(elemento),
        "nombre": nombre[:60],
        "telefono": telefono(etiquetas),
        "calle": (f"{calle}, {numero}" if numero else calle)[:120],
        "cod_postal": cod_postal,
        "localidad": localidad[:60],
        "provincia": PROVINCIAS[cod_postal[:2]],
        "categoria": tipo,
        "todo_sin_gluten": etiquetas.get("diet:gluten_free") == "only",
        "horario": horario(etiquetas.get("opening_hours")),
        "web": web(etiquetas),
        "latitud": punto[0],
        "longitud": punto[1],
    }


def _necesita_direccion(elemento: dict) -> bool:
    t = elemento.get("tags", {})
    return bool(categoria(t) and t.get("name") and coordenadas(elemento)) and not (
        t.get("addr:street") and t.get("addr:postcode") and t.get("addr:city")
    )


# ----------------------------------------------------------------- descarga e importación


def descargar() -> list[dict]:
    for url in OVERPASS:
        try:
            respuesta = httpx.post(url, data={"data": CONSULTA}, headers=AGENTE, timeout=240)
            respuesta.raise_for_status()
            return respuesta.json()["elements"]
        except (httpx.HTTPError, ValueError, KeyError) as error:
            log.warning("Overpass no responde en %s: %s", url, error)
    raise RuntimeError("Ningún servidor de Overpass ha respondido; prueba de nuevo en unos minutos.")


def direcciones(elementos: Iterable[dict], cache: Path) -> dict[str, dict]:
    """Dirección (calle, código postal, localidad) por coordenadas para los sitios que no la tienen."""
    guardadas: dict[str, dict] = json.loads(cache.read_text(encoding="utf-8")) if cache.exists() else {}
    pendientes = [e for e in elementos if _necesita_direccion(e) and osm_id(e) not in guardadas]
    for n, elemento in enumerate(pendientes, 1):
        lat, lon = coordenadas(elemento)
        try:
            respuesta = httpx.get(
                "https://nominatim.openstreetmap.org/reverse",
                params={"lat": lat, "lon": lon, "format": "jsonv2", "zoom": 18, "addressdetails": 1},
                headers=AGENTE,
                timeout=20,
            )
            respuesta.raise_for_status()
            guardadas[osm_id(elemento)] = respuesta.json().get("address", {})
        except (httpx.HTTPError, ValueError) as error:
            log.warning("Nominatim no ha devuelto la dirección de %s: %s", osm_id(elemento), error)
        if n % 25 == 0 or n == len(pendientes):
            cache.write_text(json.dumps(guardadas, ensure_ascii=False), encoding="utf-8")
            log.info("Direcciones: %d de %d", n, len(pendientes))
        time.sleep(1.1)  # política de uso de Nominatim: como mucho una petición por segundo
    return guardadas


def importar(db: Session, elementos: list[dict], direcciones_extra: dict[str, dict] | None = None) -> dict:
    """Añade los sitios que aún no están. Devuelve cuántos se han añadido y cuántos se han saltado."""
    direcciones_extra = direcciones_extra or {}
    ya = set(db.scalars(select(Establecimiento.osm_id).where(Establecimiento.osm_id.is_not(None))))
    claves = {tuple(fila) for fila in db.execute(select(Establecimiento.nombre, Establecimiento.cod_postal))}
    resultado = {"nuevos": 0, "ya_estaban": 0, "incompletos": 0}
    for elemento in elementos:
        datos = convertir(elemento, direcciones_extra.get(osm_id(elemento)))
        if datos is None:
            resultado["incompletos"] += 1
        elif datos["osm_id"] in ya or (datos["nombre"], datos["cod_postal"]) in claves:
            resultado["ya_estaban"] += 1
        else:
            db.add(Establecimiento(**datos))
            ya.add(datos["osm_id"])
            claves.add((datos["nombre"], datos["cod_postal"]))
            resultado["nuevos"] += 1
    db.commit()
    return resultado
