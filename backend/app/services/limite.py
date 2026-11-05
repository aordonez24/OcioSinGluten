"""Limita los intentos fallidos de inicio de sesión para frenar ataques de fuerza bruta.

Se guarda en memoria: suficiente para un único proceso. Con varios procesos/servidores
habría que moverlo a Redis o a la base de datos.
"""

import time
from collections import defaultdict, deque

from fastapi import HTTPException, status

MAX_FALLOS = 5
VENTANA_SEGUNDOS = 5 * 60

_fallos: dict[str, deque[float]] = defaultdict(deque)


def _limpiar(clave: str) -> deque[float]:
    registro = _fallos[clave]
    limite = time.monotonic() - VENTANA_SEGUNDOS
    while registro and registro[0] < limite:
        registro.popleft()
    return registro


def comprobar(clave: str) -> None:
    if len(_limpiar(clave)) >= MAX_FALLOS:
        raise HTTPException(
            status.HTTP_429_TOO_MANY_REQUESTS,
            "Demasiados intentos fallidos. Espera unos minutos y vuelve a probar.",
        )


def registrar_fallo(clave: str) -> None:
    _limpiar(clave).append(time.monotonic())


def reiniciar(clave: str | None = None) -> None:
    if clave is None:
        _fallos.clear()
    else:
        _fallos.pop(clave, None)
