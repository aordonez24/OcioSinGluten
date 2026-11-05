from datetime import UTC, datetime, timedelta
from typing import Annotated

import bcrypt
import jwt
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from sqlalchemy.orm import Session

from app.config import get_settings
from app.db import get_db
from app.models import Usuario

ALGORITMO = "HS256"
_bearer = HTTPBearer(auto_error=False)


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode(), bcrypt.gensalt()).decode()


def verificar_password(password: str, hash_guardado: str) -> bool:
    datos = password.encode()
    if len(datos) > 72:  # límite de bcrypt
        return False
    return bcrypt.checkpw(datos, hash_guardado.encode())


def crear_token(usuario: Usuario) -> str:
    ajustes = get_settings()
    expira = datetime.now(UTC) + timedelta(minutes=ajustes.token_expira_minutos)
    return jwt.encode({"sub": str(usuario.id), "exp": expira}, ajustes.secret_key, algorithm=ALGORITMO)


def usuario_desde_token(token: str, db: Session) -> Usuario | None:
    try:
        datos = jwt.decode(token, get_settings().secret_key, algorithms=[ALGORITMO])
        usuario = db.get(Usuario, int(datos["sub"]))
    except (jwt.PyJWTError, KeyError, ValueError):
        return None
    if usuario is None or usuario.archivado:
        return None
    return usuario


def usuario_opcional(
    credenciales: Annotated[HTTPAuthorizationCredentials | None, Depends(_bearer)],
    db: Annotated[Session, Depends(get_db)],
) -> Usuario | None:
    if credenciales is None:
        return None
    return usuario_desde_token(credenciales.credentials, db)


def usuario_actual(usuario: Annotated[Usuario | None, Depends(usuario_opcional)]) -> Usuario:
    if usuario is None:
        raise HTTPException(
            status.HTTP_401_UNAUTHORIZED,
            "Debes iniciar sesión",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return usuario


def admin_requerido(usuario: Annotated[Usuario, Depends(usuario_actual)]) -> Usuario:
    if not usuario.es_admin:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Solo disponible para administradores")
    return usuario


DB = Annotated[Session, Depends(get_db)]
UsuarioActual = Annotated[Usuario, Depends(usuario_actual)]
UsuarioOpcional = Annotated[Usuario | None, Depends(usuario_opcional)]
Admin = Annotated[Usuario, Depends(admin_requerido)]
