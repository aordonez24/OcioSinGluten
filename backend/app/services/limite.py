"""Limita cuántas veces se puede hacer algo en poco tiempo: intentos fallidos de inicio de sesión
(fuerza bruta), registros, formularios de contacto y avisos (spam).

Se guarda en memoria: suficiente para un único proceso. Con varios procesos/servidores
habría que moverlo a Redis o a la base de datos.
"""

import time
from collections import deque

from fastapi import HTTPException, Request, status

MAX_FALLOS = 5
VENTANA_SEGUNDOS = 5 * 60

_registro: dict[str, deque[float]] = {}


def _recientes(clave: str) -> deque[float]:
    marcas = _registro.get(clave, deque())
    limite = time.monotonic() - VENTANA_SEGUNDOS
    while marcas and marcas[0] < limite:
        marcas.popleft()
    if not marcas:
        _registro.pop(clave, None)  # no acumular claves vacías en memoria
    return marcas


def comprobar(clave: str, maximo: int = MAX_FALLOS, mensaje: str | None = None) -> None:
    if len(_recientes(clave)) >= maximo:
        raise HTTPException(
            status.HTTP_429_TOO_MANY_REQUESTS,
            mensaje or "Demasiados intentos fallidos. Espera unos minutos y vuelve a probar.",
        )


def registrar_fallo(clave: str) -> None:
    marcas = _recientes(clave)
    marcas.append(time.monotonic())
    _registro[clave] = marcas


def contar(clave: str, maximo: int) -> None:
    """Cuenta un uso y corta con un 429 si ya se han hecho `maximo` en la ventana de tiempo."""
    comprobar(clave, maximo, "Has hecho demasiadas peticiones seguidas. Espera unos minutos.")
    registrar_fallo(clave)


def ip(request: Request) -> str:
    """IP de quien hace la petición. Detrás del proxy del hosting, uvicorn la toma de X-Forwarded-For
    (opción --forwarded-allow-ips del Dockerfile); si no, todo el mundo compartiría el mismo límite."""
    return request.client.host if request.client else ""


def reiniciar(clave: str | None = None) -> None:
    if clave is None:
        _registro.clear()
    else:
        _registro.pop(clave, None)
