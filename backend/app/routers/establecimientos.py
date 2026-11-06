from fastapi import APIRouter, HTTPException, UploadFile, status
from sqlalchemy import delete, insert, select
from sqlalchemy.orm import Session

from app.models import (
    Comentario,
    Consulta,
    Establecimiento,
    Imagen,
    TipoActividad,
    TipoNotificacion,
    Usuario,
    ahora,
    favoritos,
    likes,
    utiles,
    visitas,
)
from app.schemas import (
    AvisoIn,
    ComentarioIn,
    ComentarioOut,
    ComentarioUpdate,
    EstablecimientoDetalle,
    EstablecimientoIn,
    EstablecimientoResumen,
    EstablecimientoUpdate,
    ImagenOut,
)
from app.security import DB, Admin, UsuarioActual, UsuarioOpcional
from app.services.consultas import (
    borrar_actividad,
    establecimiento_o_404,
    existe_fila,
    notificar,
    registrar_actividad,
)
from app.services.geocoding import geocodificar
from app.services.imagenes import borrar_imagenes, guardar_imagen

router = APIRouter(prefix="/api/establecimientos", tags=["establecimientos"])


def _puede_editar(est: Establecimiento, yo: Usuario | None) -> bool:
    return yo is not None and (yo.es_admin or est.creado_por_id == yo.id)


def _detalle(db: Session, est: Establecimiento, yo: Usuario | None) -> EstablecimientoDetalle:
    detalle = EstablecimientoDetalle.model_validate(est)
    detalle.puede_editar = _puede_editar(est, yo)
    if yo is not None:
        filtro = {"usuario_id": yo.id, "establecimiento_id": est.id}
        detalle.me_gusta = existe_fila(db, likes, **filtro)
        detalle.favorito = existe_fila(db, favoritos, **filtro)
        detalle.visitado = existe_fila(db, visitas, **filtro)
    return detalle


def _comprobar_duplicado(db: Session, nombre: str, cod_postal: str, excluir_id: int | None = None):
    consulta = select(Establecimiento.id).where(
        Establecimiento.nombre == nombre, Establecimiento.cod_postal == cod_postal
    )
    if excluir_id is not None:
        consulta = consulta.where(Establecimiento.id != excluir_id)
    if db.scalar(consulta):
        raise HTTPException(status.HTTP_409_CONFLICT, "Ese establecimiento ya está registrado")


# ----------------------------------------------------------------- CRUD


@router.get("", response_model=list[EstablecimientoResumen])
def listar(db: DB):
    return db.scalars(select(Establecimiento).order_by(Establecimiento.nombre)).all()


@router.post("", response_model=EstablecimientoDetalle, status_code=status.HTTP_201_CREATED)
def crear(datos: EstablecimientoIn, db: DB, yo: UsuarioActual):
    _comprobar_duplicado(db, datos.nombre, datos.cod_postal)
    est = Establecimiento(**datos.model_dump(), creado_por_id=yo.id)
    geocodificar(est)
    db.add(est)
    db.flush()
    registrar_actividad(db, yo.id, est.id, TipoActividad.PUBLICACION)
    db.commit()
    db.refresh(est)
    return _detalle(db, est, yo)


@router.get("/{est_id}", response_model=EstablecimientoDetalle)
def obtener(est_id: int, db: DB, yo: UsuarioOpcional):
    return _detalle(db, establecimiento_o_404(db, est_id), yo)


@router.patch("/{est_id}", response_model=EstablecimientoDetalle)
def editar(est_id: int, datos: EstablecimientoUpdate, db: DB, yo: UsuarioActual):
    est = establecimiento_o_404(db, est_id)
    if not _puede_editar(est, yo):
        raise HTTPException(
            status.HTTP_403_FORBIDDEN, "Solo quien lo añadió o el equipo de administración puede editarlo"
        )
    cambios = datos.model_dump(exclude_unset=True, exclude_none=True)
    # Una cadena vacía, un precio 0 o un horario [] significan "quitar este dato".
    for campo in ("precio", "horario", "nota_horario", "web"):
        if campo in cambios and not cambios[campo]:
            cambios[campo] = None
    _comprobar_duplicado(
        db, cambios.get("nombre", est.nombre), cambios.get("cod_postal", est.cod_postal), est.id
    )
    for campo, valor in cambios.items():
        setattr(est, campo, valor)
    if cambios.keys() & {"calle", "cod_postal", "localidad", "provincia", "pais"}:
        geocodificar(est)
    db.commit()
    db.refresh(est)
    return _detalle(db, est, yo)


@router.delete("/{est_id}", status_code=status.HTTP_204_NO_CONTENT)
def eliminar(est_id: int, db: DB, admin: Admin):
    est = establecimiento_o_404(db, est_id)
    ficheros = [img.fichero for img in est.imagenes]
    db.delete(est)  # comentarios, imágenes, likes, actividad... se borran en cascada
    db.commit()
    borrar_imagenes(*ficheros)


@router.post("/{est_id}/avisos", status_code=status.HTTP_201_CREATED)
def avisar(est_id: int, datos: AvisoIn, db: DB, yo: UsuarioActual) -> dict:
    """Avisa al equipo de administración de un problema con el sitio (cerrado, datos mal…)."""
    establecimiento_o_404(db, est_id)
    db.add(
        Consulta(
            nombre=yo.username,
            email=yo.email,
            mensaje=datos.mensaje,
            motivo=datos.motivo,
            establecimiento_id=est_id,
        )
    )
    db.commit()
    return {"ok": True}


# ----------------------------------------------------------------- imágenes


@router.post("/{est_id}/imagenes", response_model=ImagenOut, status_code=status.HTTP_201_CREATED)
def subir_imagen(est_id: int, archivo: UploadFile, db: DB, yo: UsuarioActual):
    establecimiento_o_404(db, est_id)
    imagen = Imagen(establecimiento_id=est_id, autor_id=yo.id, fichero=guardar_imagen(archivo))
    db.add(imagen)
    db.commit()
    return imagen


@router.delete("/{est_id}/imagenes/{imagen_id}", status_code=status.HTTP_204_NO_CONTENT)
def borrar_imagen(est_id: int, imagen_id: int, db: DB, yo: UsuarioActual):
    imagen = db.get(Imagen, imagen_id)
    if imagen is None or imagen.establecimiento_id != est_id:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Imagen no encontrada")
    if imagen.autor_id != yo.id and not yo.es_admin:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Solo puedes borrar tus propias imágenes")
    db.delete(imagen)
    db.commit()
    borrar_imagenes(imagen.fichero)


# ----------------------------------------------------------------- like / favorito / visita


def _marcar(db: Session, tabla, est_id: int, yo: Usuario, activo: bool, tipo: TipoActividad | None):
    """Activa o desactiva una marca usuario-establecimiento. Es idempotente."""
    establecimiento_o_404(db, est_id)
    filtro = {"usuario_id": yo.id, "establecimiento_id": est_id}
    ya_existe = existe_fila(db, tabla, **filtro)
    if activo and not ya_existe:
        db.execute(insert(tabla).values(**filtro))
        if tipo:
            registrar_actividad(db, yo.id, est_id, tipo)
    elif not activo and ya_existe:
        db.execute(delete(tabla).filter_by(**filtro))
        if tipo:
            borrar_actividad(db, yo.id, est_id, tipo)
    db.commit()


def _rutas_marca(nombre: str, tabla, tipo: TipoActividad | None) -> None:
    ruta = f"/{{est_id}}/{nombre}"

    @router.put(ruta, status_code=status.HTTP_204_NO_CONTENT, name=f"marcar_{nombre}")
    def activar(est_id: int, db: DB, yo: UsuarioActual):
        _marcar(db, tabla, est_id, yo, True, tipo)

    @router.delete(ruta, status_code=status.HTTP_204_NO_CONTENT, name=f"desmarcar_{nombre}")
    def desactivar(est_id: int, db: DB, yo: UsuarioActual):
        _marcar(db, tabla, est_id, yo, False, tipo)


_rutas_marca("like", likes, TipoActividad.LIKE)
_rutas_marca("favorito", favoritos, None)
_rutas_marca("visita", visitas, TipoActividad.VISITA)


# ----------------------------------------------------------------- comentarios


@router.get("/{est_id}/comentarios", response_model=list[ComentarioOut])
def comentarios(est_id: int, db: DB, yo: UsuarioOpcional):
    establecimiento_o_404(db, est_id)
    lista = db.scalars(
        select(Comentario)
        .join(Comentario.autor)
        .where(Comentario.establecimiento_id == est_id, Usuario.archivado.is_(False))
        .order_by(Comentario.creado_en.desc(), Comentario.id.desc())
    ).all()
    mis_utiles = set()
    if yo is not None:
        mis_utiles = set(db.scalars(select(utiles.c.comentario_id).where(utiles.c.usuario_id == yo.id)))
    return [ComentarioOut.model_validate(c).model_copy(update={"util": c.id in mis_utiles}) for c in lista]


@router.post("/{est_id}/comentarios", response_model=ComentarioOut, status_code=status.HTTP_201_CREATED)
def opinar(est_id: int, datos: ComentarioIn, db: DB, yo: UsuarioActual):
    est = establecimiento_o_404(db, est_id)
    ya_opino = db.scalar(
        select(Comentario.id).where(Comentario.establecimiento_id == est_id, Comentario.autor_id == yo.id)
    )
    if ya_opino:
        raise HTTPException(
            status.HTTP_409_CONFLICT, "Ya has opinado sobre este sitio; puedes editar tu opinión"
        )
    comentario = Comentario(establecimiento_id=est_id, autor_id=yo.id, **datos.model_dump())
    db.add(comentario)
    registrar_actividad(db, yo.id, est_id, TipoActividad.COMENTARIO)
    notificar(db, est.creado_por_id, yo.id, TipoNotificacion.OPINION, est_id)
    db.commit()
    db.refresh(comentario)
    return comentario


comentarios_router = APIRouter(prefix="/api/comentarios", tags=["establecimientos"])


def _opinion_propia(db: Session, comentario_id: int, yo: Usuario, *, admin_puede: bool) -> Comentario:
    comentario = db.get(Comentario, comentario_id)
    if comentario is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Opinión no encontrada")
    if comentario.autor_id != yo.id and not (admin_puede and yo.es_admin):
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Solo puedes modificar tus propias opiniones")
    return comentario


@comentarios_router.patch("/{comentario_id}", response_model=ComentarioOut)
def editar_opinion(comentario_id: int, datos: ComentarioUpdate, db: DB, yo: UsuarioActual):
    comentario = _opinion_propia(db, comentario_id, yo, admin_puede=False)
    cambios = datos.model_dump(exclude_unset=True, exclude_none=True)
    if cambios:
        for campo, valor in cambios.items():
            setattr(comentario, campo, valor)
        comentario.editado_en = ahora()
        db.commit()
    return comentario


def _marcar_util(db: Session, comentario_id: int, yo: Usuario, activo: bool) -> None:
    comentario = db.get(Comentario, comentario_id)
    if comentario is None:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Opinión no encontrada")
    if comentario.autor_id == yo.id:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "No puedes valorar tu propia opinión")
    filtro = {"usuario_id": yo.id, "comentario_id": comentario_id}
    ya_existe = existe_fila(db, utiles, **filtro)
    if activo and not ya_existe:
        db.execute(insert(utiles).values(**filtro))
        notificar(db, comentario.autor_id, yo.id, TipoNotificacion.UTIL, comentario.establecimiento_id)
    elif not activo and ya_existe:
        db.execute(delete(utiles).filter_by(**filtro))
    db.commit()


@comentarios_router.put("/{comentario_id}/util", status_code=status.HTTP_204_NO_CONTENT)
def marcar_util(comentario_id: int, db: DB, yo: UsuarioActual):
    _marcar_util(db, comentario_id, yo, True)


@comentarios_router.delete("/{comentario_id}/util", status_code=status.HTTP_204_NO_CONTENT)
def desmarcar_util(comentario_id: int, db: DB, yo: UsuarioActual):
    _marcar_util(db, comentario_id, yo, False)


@comentarios_router.delete("/{comentario_id}", status_code=status.HTTP_204_NO_CONTENT)
def borrar_opinion(comentario_id: int, db: DB, yo: UsuarioActual):
    comentario = _opinion_propia(db, comentario_id, yo, admin_puede=True)
    borrar_actividad(db, comentario.autor_id, comentario.establecimiento_id, TipoActividad.COMENTARIO)
    db.delete(comentario)
    db.commit()
