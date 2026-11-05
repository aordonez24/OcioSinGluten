from fastapi import APIRouter, HTTPException, UploadFile, status
from sqlalchemy import delete, func, insert, select
from sqlalchemy.orm import joinedload, undefer

from app.models import (
    Comentario,
    Establecimiento,
    TipoNotificacion,
    Usuario,
    favoritos,
    seguimientos,
    visitas,
)
from app.schemas import (
    ArchivadoIn,
    EstablecimientoResumen,
    OpinionDePerfil,
    PasswordCambio,
    UsuarioComunidad,
    UsuarioPerfil,
    UsuarioPrivado,
    UsuarioResumen,
    UsuarioUpdate,
)
from app.security import (
    DB,
    Admin,
    UsuarioActual,
    UsuarioOpcional,
    hash_password,
    verificar_password,
)
from app.services.consultas import existe_fila, notificar, usuario_por_username
from app.services.imagenes import borrar_imagenes, guardar_imagen

router = APIRouter(prefix="/api/usuarios", tags=["usuarios"])


@router.get("", response_model=list[UsuarioComunidad])
def listar(db: DB, yo: UsuarioActual):
    consulta = (
        select(Usuario)
        .options(undefer(Usuario.num_seguidores))
        .where(Usuario.id != yo.id)
        .order_by(Usuario.num_seguidores.desc(), Usuario.username)
    )
    if not yo.es_admin:  # los administradores también ven las cuentas suspendidas
        consulta = consulta.where(Usuario.archivado.is_(False))
    sigo = set(db.scalars(select(seguimientos.c.seguido_id).where(seguimientos.c.seguidor_id == yo.id)))
    return [
        UsuarioComunidad.model_validate(u).model_copy(update={"siguiendo": u.id in sigo})
        for u in db.scalars(consulta)
    ]


# ----------------------------------------------------------------- mi cuenta


@router.patch("/yo", response_model=UsuarioPrivado)
def actualizar_mis_datos(datos: UsuarioUpdate, db: DB, yo: UsuarioActual):
    cambios = datos.model_dump(exclude_unset=True, exclude_none=True)
    nuevo_username = cambios.get("username")
    if (
        nuevo_username
        and nuevo_username != yo.username
        and db.scalar(select(Usuario.id).where(Usuario.username == nuevo_username))
    ):
        raise HTTPException(status.HTTP_409_CONFLICT, "Ese nombre de usuario ya está en uso")
    for campo, valor in cambios.items():
        setattr(yo, campo, valor)
    db.commit()
    return yo


@router.put("/yo/password", status_code=status.HTTP_204_NO_CONTENT)
def cambiar_password(datos: PasswordCambio, db: DB, yo: UsuarioActual):
    if not verificar_password(datos.actual, yo.password_hash):
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "La contraseña actual no es correcta")
    yo.password_hash = hash_password(datos.nueva)
    db.commit()


@router.put("/yo/foto", response_model=UsuarioPrivado)
def subir_foto(archivo: UploadFile, db: DB, yo: UsuarioActual):
    anterior = yo.foto
    yo.foto = guardar_imagen(archivo, max_lado=400)
    db.commit()
    borrar_imagenes(anterior)
    return yo


@router.delete("/yo/foto", response_model=UsuarioPrivado)
def borrar_foto(db: DB, yo: UsuarioActual):
    anterior, yo.foto = yo.foto, None
    db.commit()
    borrar_imagenes(anterior)
    return yo


# ----------------------------------------------------------------- perfiles


@router.get("/{username}", response_model=UsuarioPerfil)
def perfil(username: str, db: DB, yo: UsuarioOpcional):
    es_admin = bool(yo and yo.es_admin)
    usuario = usuario_por_username(db, username, incluir_archivados=es_admin)
    puede_ver_privado = yo is not None and (yo.id == usuario.id or es_admin)

    def contar(columna) -> int:
        return db.scalar(select(func.count()).where(columna == usuario.id))

    return UsuarioPerfil(
        username=usuario.username,
        nombre=usuario.nombre,
        apellidos=usuario.apellidos,
        archivado=usuario.archivado,
        foto=usuario.foto,
        rol=usuario.rol,
        creado_en=usuario.creado_en,
        num_seguidores=usuario.num_seguidores,
        num_seguidos=usuario.num_seguidos,
        num_opiniones=contar(Comentario.autor_id),
        num_visitados=contar(visitas.c.usuario_id),
        siguiendo=yo is not None and existe_fila(db, seguimientos, seguidor_id=yo.id, seguido_id=usuario.id),
        privado=UsuarioPrivado.model_validate(usuario) if puede_ver_privado else None,
    )


def _usuarios_relacionados(db: DB, username: str, columna_filtro, columna_resultado):
    usuario = usuario_por_username(db, username)
    return db.scalars(
        select(Usuario)
        .join(seguimientos, columna_resultado == Usuario.id)
        .where(columna_filtro == usuario.id, Usuario.archivado.is_(False))
        .order_by(Usuario.username)
    ).all()


@router.get("/{username}/seguidores", response_model=list[UsuarioResumen])
def seguidores(username: str, db: DB):
    return _usuarios_relacionados(db, username, seguimientos.c.seguido_id, seguimientos.c.seguidor_id)


@router.get("/{username}/seguidos", response_model=list[UsuarioResumen])
def seguidos(username: str, db: DB):
    return _usuarios_relacionados(db, username, seguimientos.c.seguidor_id, seguimientos.c.seguido_id)


@router.put("/{username}/seguir", status_code=status.HTTP_204_NO_CONTENT)
def seguir(username: str, db: DB, yo: UsuarioActual):
    otro = usuario_por_username(db, username)
    if otro.id == yo.id:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "No puedes seguirte a ti mismo")
    if not existe_fila(db, seguimientos, seguidor_id=yo.id, seguido_id=otro.id):
        db.execute(insert(seguimientos).values(seguidor_id=yo.id, seguido_id=otro.id))
        notificar(db, otro.id, yo.id, TipoNotificacion.SEGUIDOR)
        db.commit()


@router.delete("/{username}/seguir", status_code=status.HTTP_204_NO_CONTENT)
def dejar_de_seguir(username: str, db: DB, yo: UsuarioActual):
    otro = usuario_por_username(db, username, incluir_archivados=True)
    db.execute(
        delete(seguimientos).where(seguimientos.c.seguidor_id == yo.id, seguimientos.c.seguido_id == otro.id)
    )
    db.commit()


def _establecimientos_de(db: DB, username: str, tabla):
    usuario = usuario_por_username(db, username)
    return db.scalars(
        select(Establecimiento)
        .join(tabla, tabla.c.establecimiento_id == Establecimiento.id)
        .where(tabla.c.usuario_id == usuario.id)
        .order_by(tabla.c.creado_en.desc())
    ).all()


@router.get("/{username}/favoritos", response_model=list[EstablecimientoResumen])
def favoritos_de(username: str, db: DB):
    return _establecimientos_de(db, username, favoritos)


@router.get("/{username}/visitados", response_model=list[EstablecimientoResumen])
def visitados_de(username: str, db: DB):
    return _establecimientos_de(db, username, visitas)


@router.get("/{username}/opiniones", response_model=list[OpinionDePerfil])
def opiniones_de(username: str, db: DB):
    usuario = usuario_por_username(db, username)
    return db.scalars(
        select(Comentario)
        .options(joinedload(Comentario.establecimiento))
        .where(Comentario.autor_id == usuario.id)
        .order_by(Comentario.creado_en.desc())
    ).all()


# ----------------------------------------------------------------- moderación


@router.patch("/{username}/archivado", response_model=UsuarioResumen)
def suspender(username: str, datos: ArchivadoIn, db: DB, admin: Admin):
    usuario = usuario_por_username(db, username, incluir_archivados=True)
    if usuario.id == admin.id:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "No puedes suspender tu propia cuenta")
    usuario.archivado = datos.archivado
    db.commit()
    return usuario


@router.delete("/{username}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar(username: str, db: DB, yo: UsuarioActual):
    usuario = usuario_por_username(db, username, incluir_archivados=True)
    if usuario.id != yo.id and not yo.es_admin:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "No puedes eliminar la cuenta de otra persona")
    foto = usuario.foto
    db.delete(usuario)  # seguimientos, likes, visitas, comentarios... se borran en cascada
    db.commit()
    borrar_imagenes(foto)
