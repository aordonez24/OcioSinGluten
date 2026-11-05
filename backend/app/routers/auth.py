from fastapi import APIRouter, HTTPException, Request, status
from sqlalchemy import select

from app.models import Usuario
from app.schemas import LoginIn, RegistroIn, TokenOut, UsuarioPrivado
from app.security import DB, UsuarioActual, crear_token, hash_password, verificar_password
from app.services import limite

router = APIRouter(prefix="/api/auth", tags=["auth"])


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
    clave = f"{email}|{request.client.host if request.client else ''}"
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
