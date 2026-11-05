"""Chat grupal en tiempo real con WebSockets nativos (sustituye a STOMP + SockJS).

Los mensajes se guardan en la base de datos para que el historial sobreviva a reinicios.
"""

from fastapi import APIRouter, WebSocket, WebSocketDisconnect, status
from fastapi.concurrency import run_in_threadpool
from sqlalchemy import select

from app.db import SessionLocal
from app.models import Mensaje
from app.schemas import MensajeOut
from app.security import usuario_desde_token

router = APIRouter()

MAX_LONGITUD = 500
HISTORIAL = 50


class Sala:
    def __init__(self) -> None:
        self.conexiones: set[WebSocket] = set()

    async def difundir(self, mensaje: dict) -> None:
        for conexion in list(self.conexiones):
            try:
                await conexion.send_json(mensaje)
            except (WebSocketDisconnect, RuntimeError):
                self.conexiones.discard(conexion)


sala = Sala()


def _autenticar(token: str) -> int | None:
    with SessionLocal() as db:
        usuario = usuario_desde_token(token, db)
        return usuario.id if usuario else None


def _historial() -> list[dict]:
    with SessionLocal() as db:
        ultimos = db.scalars(select(Mensaje).order_by(Mensaje.id.desc()).limit(HISTORIAL)).all()
        return [MensajeOut.model_validate(m).model_dump(mode="json") for m in reversed(ultimos)]


def _guardar(autor_id: int, texto: str) -> dict:
    with SessionLocal() as db:
        mensaje = Mensaje(autor_id=autor_id, texto=texto)
        db.add(mensaje)
        db.commit()
        db.refresh(mensaje)
        return MensajeOut.model_validate(mensaje).model_dump(mode="json")


@router.websocket("/api/chat")
async def chat(websocket: WebSocket, token: str = ""):
    # El navegador no puede mandar cabeceras en un WebSocket, así que el token va en la URL.
    autor_id = await run_in_threadpool(_autenticar, token)
    if autor_id is None:
        await websocket.close(code=status.WS_1008_POLICY_VIOLATION)
        return

    await websocket.accept()
    await websocket.send_json({"tipo": "historial", "mensajes": await run_in_threadpool(_historial)})
    sala.conexiones.add(websocket)
    try:
        while True:
            texto = str((await websocket.receive_json()).get("texto", "")).strip()[:MAX_LONGITUD]
            if texto:
                mensaje = await run_in_threadpool(_guardar, autor_id, texto)
                await sala.difundir({"tipo": "mensaje", **mensaje})
    except (WebSocketDisconnect, ValueError, AttributeError):
        pass
    finally:
        sala.conexiones.discard(websocket)
