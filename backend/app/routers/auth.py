from fastapi import APIRouter, BackgroundTasks, HTTPException, Request, status
from sqlalchemy import select

from app.models import Usuario
from app.schemas import LoginIn, RecuperarIn, RegistroIn, RestablecerIn, TokenOut, UsuarioPrivado
from app.security import (
    DB,
    UsuarioActual,
    crear_token,
    crear_token_recuperacion,
    hash_password,
    usuario_desde_token_recuperacion,
    verificar_password,
)
from app.services import correo, limite
from app.services.compartir import url_base

router = APIRouter(prefix="/api/auth", tags=["auth"])


def _ip(request: Request) -> str:
    return request.client.host if request.client else ""


@router.post("/registro", response_model=TokenOut, status_code=status.HTTP_201_CREATED)
def registro(datos: RegistroIn, db: DB):
    email = datos.email.lower()
    for campo, valor, mensaje in (
        (Usuario.dni, datos.dni, "Ya existe una cuenta con ese DNI"),
        (Usuario.email, email, "Ya existe una cuenta con ese email"),
        (Usuario.username, datos.username, "Ese nombre de usuario ya está en uso"),
    ):
        if db.scalar(select(Usuario.id).where(campo == valor)):
            raise HTTPException(status.HTTP_409_CONFLICT, mensaje)

    usuario = Usuario(
        **datos.model_dump(exclude={"password", "email"}),
        email=email,
        password_hash=hash_password(datos.password),
    )
    db.add(usuario)
    db.commit()
    return TokenOut(access_token=crear_token(usuario), usuario=usuario)


@router.post("/login", response_model=TokenOut)
def login(datos: LoginIn, db: DB, request: Request):
    email = datos.email.strip().lower()
    clave = f"{email}|{_ip(request)}"
    limite.comprobar(clave)
    usuario = db.scalar(select(Usuario).where(Usuario.email == email))
    # Mismo mensaje exista o no el email, para no revelar qué cuentas hay registradas.
    if usuario is None or not verificar_password(datos.password, usuario.password_hash):
        limite.registrar_fallo(clave)
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "Email o contraseña incorrectos")
    limite.reiniciar(clave)
    if usuario.archivado:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Tu cuenta está suspendida")
    return TokenOut(access_token=crear_token(usuario), usuario=usuario)


@router.get("/yo", response_model=UsuarioPrivado)
def yo(usuario: UsuarioActual):
    return usuario


TEXTO_RECUPERAR = """Hola, {nombre}:

Has pedido cambiar la contraseña de tu cuenta en Ocio Sin Gluten. Para elegir una nueva, abre este
enlace antes de una hora:

{enlace}

Si no lo has pedido tú, no hagas nada: tu contraseña sigue siendo la misma.

Ocio Sin Gluten
"""


@router.post("/recuperar", status_code=status.HTTP_202_ACCEPTED)
def recuperar(datos: RecuperarIn, db: DB, request: Request, tareas: BackgroundTasks) -> dict:
    """Envía un enlace para elegir una contraseña nueva. Responde lo mismo exista o no la cuenta."""
    clave = f"recuperar|{_ip(request)}"
    limite.comprobar(clave)
    limite.registrar_fallo(clave)  # cuenta cada petición: como mucho 5 cada pocos minutos

    usuario = db.scalar(select(Usuario).where(Usuario.email == datos.email.strip().lower()))
    if usuario is not None and not usuario.archivado:
        enlace = f"{url_base(request)}/restablecer?token={crear_token_recuperacion(usuario)}"
        texto = TEXTO_RECUPERAR.format(nombre=usuario.nombre, enlace=enlace)
        # En segundo plano: la respuesta tarda lo mismo exista o no la cuenta.
        tareas.add_task(correo.enviar, usuario.email, "Cambia tu contraseña de Ocio Sin Gluten", texto)
    return {"ok": True}


@router.post("/restablecer", response_model=TokenOut)
def restablecer(datos: RestablecerIn, db: DB):
    """Guarda la contraseña nueva e inicia sesión. Cada enlace sirve una sola vez."""
    usuario = usuario_desde_token_recuperacion(datos.token, db)
    if usuario is None:
        raise HTTPException(
            status.HTTP_400_BAD_REQUEST, "El enlace ya no es válido o ha caducado. Pide uno nuevo."
        )
    usuario.password_hash = hash_password(datos.password)
    db.commit()
    return TokenOut(access_token=crear_token(usuario), usuario=usuario)
