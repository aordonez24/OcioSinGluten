import re
from datetime import UTC, date, datetime
from typing import Annotated

from pydantic import (
    AfterValidator,
    BaseModel,
    ConfigDict,
    EmailStr,
    Field,
    StringConstraints,
    computed_field,
)

from app.models import Categoria, MotivoAviso, Rol, TipoActividad

# --------------------------------------------------------------------------- validadores

LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE"


def _dni(valor: str) -> str:
    valor = valor.strip().upper()
    if not re.fullmatch(r"\d{8}[A-Z]", valor) or LETRAS_DNI[int(valor[:8]) % 23] != valor[8]:
        raise ValueError("DNI no válido")
    return valor


def _telefono(valor: str) -> str:
    valor = re.sub(r"[\s-]", "", valor)
    if not re.fullmatch(r"\d{9}", valor):
        raise ValueError("El teléfono debe tener 9 dígitos")
    return valor


def _password(valor: str) -> str:
    if len(valor) < 8 or not re.search(r"[A-Z]", valor) or not re.search(r"\d", valor):
        raise ValueError("La contraseña necesita al menos 8 caracteres, una mayúscula y un número")
    if len(valor.encode()) > 72:
        raise ValueError("La contraseña es demasiado larga")
    return valor


def _mayor_de_edad(valor: date) -> date:
    hoy = date.today()
    edad = hoy.year - valor.year - ((hoy.month, hoy.day) < (valor.month, valor.day))
    if edad < 18:
        raise ValueError("Debes ser mayor de edad")
    return valor


def _cod_postal(valor: str) -> str:
    if not re.fullmatch(r"\d{5}", valor.strip()):
        raise ValueError("El código postal debe tener 5 dígitos")
    return valor.strip()


def _web(valor: str) -> str:
    valor = valor.strip()
    if not re.match(r"^https?://", valor, re.IGNORECASE):
        valor = f"https://{valor}"
    if not re.fullmatch(r"https?://[^\s/$.?#][^\s]*\.[^\s]{2,}", valor, re.IGNORECASE):
        raise ValueError("La web no parece una dirección válida")
    return valor


def _texto(max_len: int):
    return Annotated[str, StringConstraints(strip_whitespace=True, min_length=1, max_length=max_len)]


DNI = Annotated[str, AfterValidator(_dni)]
Telefono = Annotated[str, AfterValidator(_telefono)]
Password = Annotated[str, AfterValidator(_password)]
FechaNacimiento = Annotated[date, AfterValidator(_mayor_de_edad)]
CodPostal = Annotated[str, AfterValidator(_cod_postal)]
# SQLite no guarda la zona horaria: las fechas se guardan en UTC y se marcan como tal al salir.
FechaUTC = Annotated[datetime, AfterValidator(lambda d: d if d.tzinfo else d.replace(tzinfo=UTC))]
Username = Annotated[str, Field(min_length=3, max_length=30, pattern=r"^[A-Za-z0-9_.-]+$")]
Web = Annotated[str, StringConstraints(max_length=200), AfterValidator(_web)]
Precio = Annotated[int, Field(ge=1, le=3, description="1 económico, 2 normal, 3 caro")]


def url_upload(fichero: str | None) -> str | None:
    return f"/uploads/{fichero}" if fichero else None


class Esquema(BaseModel):
    model_config = ConfigDict(from_attributes=True)


# --------------------------------------------------------------------------- usuarios


class RegistroIn(BaseModel):
    dni: DNI
    username: Username
    nombre: _texto(50)
    apellidos: _texto(80)
    fecha_nacimiento: FechaNacimiento
    telefono: Telefono
    email: EmailStr
    password: Password


class LoginIn(BaseModel):
    email: str
    password: str


class UsuarioUpdate(BaseModel):
    username: Username | None = None
    nombre: _texto(50) | None = None
    apellidos: _texto(80) | None = None
    fecha_nacimiento: FechaNacimiento | None = None
    telefono: Telefono | None = None


class PasswordCambio(BaseModel):
    actual: str
    nueva: Password


class ArchivadoIn(BaseModel):
    archivado: bool


class UsuarioResumen(Esquema):
    username: str
    nombre: str
    archivado: bool
    foto: str | None = Field(exclude=True)

    @computed_field
    def foto_url(self) -> str | None:
        return url_upload(self.foto)


class UsuarioPrivado(UsuarioResumen):
    """Todos los datos: solo para el propio usuario o un administrador."""

    id: int
    apellidos: str
    rol: Rol
    email: str
    dni: str
    telefono: str
    fecha_nacimiento: date
    creado_en: FechaUTC


class UsuarioPerfil(UsuarioResumen):
    apellidos: str
    rol: Rol
    creado_en: FechaUTC
    num_seguidores: int
    num_seguidos: int
    num_opiniones: int
    num_visitados: int
    siguiendo: bool
    # Datos personales: solo se rellenan si quien mira es el propio usuario o un admin.
    privado: UsuarioPrivado | None = None


class UsuarioComunidad(UsuarioResumen):
    num_seguidores: int
    siguiendo: bool = False


class TokenOut(BaseModel):
    access_token: str
    token_type: str = "bearer"
    usuario: UsuarioPrivado


# --------------------------------------------------------------------------- establecimientos


class EstablecimientoIn(BaseModel):
    nombre: _texto(60)
    telefono: Telefono
    calle: _texto(120)
    cod_postal: CodPostal
    localidad: _texto(60)
    provincia: _texto(60)
    pais: _texto(60) = "España"
    categoria: Categoria = Categoria.RESTAURANTE
    todo_sin_gluten: bool = False
    carta_sin_gluten: bool = False
    cocina_separada: bool = False
    acreditado: bool = False
    precio: Precio | None = None
    horario: Annotated[str, StringConstraints(strip_whitespace=True, max_length=300)] | None = None
    web: Web | None = None


class EstablecimientoUpdate(BaseModel):
    nombre: _texto(60) | None = None
    telefono: Telefono | None = None
    calle: _texto(120) | None = None
    cod_postal: CodPostal | None = None
    localidad: _texto(60) | None = None
    provincia: _texto(60) | None = None
    pais: _texto(60) | None = None
    categoria: Categoria | None = None
    todo_sin_gluten: bool | None = None
    carta_sin_gluten: bool | None = None
    cocina_separada: bool | None = None
    acreditado: bool | None = None
    # Para borrar uno de estos datos se envía una cadena vacía (o 0 en el precio).
    precio: Annotated[int, Field(ge=0, le=3)] | None = None
    horario: Annotated[str, StringConstraints(strip_whitespace=True, max_length=300)] | None = None
    web: Web | Annotated[str, StringConstraints(max_length=0)] | None = None


class EstablecimientoResumen(Esquema):
    id: int
    nombre: str
    telefono: str
    calle: str
    cod_postal: str
    localidad: str
    provincia: str
    pais: str
    categoria: Categoria
    todo_sin_gluten: bool
    carta_sin_gluten: bool
    cocina_separada: bool
    acreditado: bool
    precio: int | None
    num_likes: int
    num_visitas: int
    num_opiniones: int
    valoracion: float | None
    latitud: float | None
    longitud: float | None
    portada: str | None = Field(exclude=True)

    @computed_field
    def portada_url(self) -> str | None:
        return url_upload(self.portada)


class ImagenOut(Esquema):
    id: int
    autor_id: int | None
    fichero: str = Field(exclude=True)

    @computed_field
    def url(self) -> str:
        return url_upload(self.fichero)


class EstablecimientoDetalle(EstablecimientoResumen):
    horario: str | None
    web: str | None
    creado_por: UsuarioResumen | None
    creado_en: FechaUTC
    imagenes: list[ImagenOut]
    # Estado respecto al usuario que consulta (False si no hay sesión).
    me_gusta: bool = False
    favorito: bool = False
    visitado: bool = False
    puede_editar: bool = False  # quien lo añadió o el equipo de administración


class EstablecimientoMini(Esquema):
    id: int
    nombre: str
    localidad: str
    provincia: str


# ------------------------------------------------------------- comentarios, actividad, chat, consultas


Puntuacion = Annotated[int, Field(ge=1, le=5, description="Estrellas de 1 a 5")]


class ComentarioIn(BaseModel):
    puntuacion: Puntuacion
    mensaje: _texto(500)


class ComentarioUpdate(BaseModel):
    puntuacion: Puntuacion | None = None
    mensaje: _texto(500) | None = None


class ComentarioOut(Esquema):
    id: int
    puntuacion: int
    mensaje: str
    creado_en: FechaUTC
    editado_en: FechaUTC | None
    autor: UsuarioResumen


class OpinionDePerfil(Esquema):
    """Opinión vista desde el perfil de su autor: con el sitio en lugar de la persona."""

    id: int
    puntuacion: int
    mensaje: str
    creado_en: FechaUTC
    editado_en: FechaUTC | None
    establecimiento: EstablecimientoMini


class ActividadOut(Esquema):
    id: int
    tipo: TipoActividad
    creado_en: FechaUTC
    usuario: UsuarioResumen
    establecimiento: EstablecimientoMini


class MensajeOut(Esquema):
    id: int
    texto: str
    creado_en: FechaUTC
    autor: UsuarioResumen


class Estadisticas(BaseModel):
    establecimientos: int
    miembros: int
    opiniones: int
    provincias: int


class ConsultaIn(BaseModel):
    nombre: _texto(60)
    email: EmailStr
    mensaje: _texto(1000)


class AvisoIn(BaseModel):
    motivo: MotivoAviso
    mensaje: Annotated[str, StringConstraints(strip_whitespace=True, max_length=1000)] = ""


class ConsultaOut(Esquema):
    id: int
    nombre: str
    email: str
    mensaje: str
    creado_en: FechaUTC
    motivo: MotivoAviso | None = None
    establecimiento: EstablecimientoMini | None = None
