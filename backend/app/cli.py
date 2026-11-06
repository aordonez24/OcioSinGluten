"""Utilidades de línea de comandos.

python -m app.cli admin correo@ejemplo.com   # convierte una cuenta en administradora
python -m app.cli demo                       # carga datos de ejemplo en una BD vacía
python -m app.cli migrar                     # actualiza el esquema (también se hace al arrancar)
"""

import argparse
import json
import random
import shutil
import uuid
from datetime import date, timedelta
from pathlib import Path

from sqlalchemy import insert, select

from app.config import get_settings
from app.db import SessionLocal, migrar
from app.models import (
    Actividad,
    Categoria,
    Comentario,
    Consulta,
    Establecimiento,
    Imagen,
    Mensaje,
    MotivoAviso,
    Notificacion,
    Rol,
    TipoActividad,
    TipoNotificacion,
    Usuario,
    ahora,
    favoritos,
    likes,
    seguimientos,
    utiles,
    visitas,
)
from app.schemas import LETRAS_DNI
from app.security import hash_password


def hacer_admin(email: str) -> None:
    with SessionLocal() as db:
        usuario = db.scalar(select(Usuario).where(Usuario.email == email.lower()))
        if usuario is None:
            raise SystemExit(f"No existe ninguna cuenta con el email {email}")
        usuario.rol = Rol.ADMIN
        db.commit()
    print(f"{usuario.username} ahora es administrador.")


USUARIOS = [
    # username, nombre, apellidos, contraseña, rol
    ("admin", "Admin", "Ocio Sin Gluten", "Admin1234", Rol.ADMIN),
    ("lucia", "Lucía", "García Ruiz", "Lucia1234", Rol.COMUN),
    ("marcos", "Marcos", "López Martín", "Marcos1234", Rol.COMUN),
    ("sara", "Sara", "Jiménez Torres", "Sara12345", Rol.COMUN),
    ("javi", "Javier", "Moreno Díaz", "Javi12345", Rol.COMUN),
]

ESTABLECIMIENTOS = [
    # nombre, calle, cp, localidad, provincia, lat, lon, categoría, distintivos
    (
        "Celiaco Feliz",
        "Calle Maestra 10",
        "23002",
        "Jaén",
        "Jaén",
        37.7692,
        -3.7903,
        Categoria.RESTAURANTE,
        {"todo_sin_gluten", "acreditado"},
    ),
    (
        "Horno Sin Trigo",
        "Calle Reyes Católicos 5",
        "18009",
        "Granada",
        "Granada",
        37.1760,
        -3.5986,
        Categoria.PANADERIA,
        {"todo_sin_gluten", "acreditado"},
    ),
    (
        "La Pizzería Libre",
        "Calle Larios 2",
        "29005",
        "Málaga",
        "Málaga",
        36.7194,
        -4.4211,
        Categoria.RESTAURANTE,
        {"carta_sin_gluten", "cocina_separada"},
    ),
    (
        "Taberna El Arroz",
        "Calle Sierpes 40",
        "41004",
        "Sevilla",
        "Sevilla",
        37.3925,
        -5.9945,
        Categoria.BAR,
        {"carta_sin_gluten"},
    ),
    (
        "Burger Zero",
        "Calle de Fuencarral 70",
        "28004",
        "Madrid",
        "Madrid",
        40.4255,
        -3.7020,
        Categoria.RESTAURANTE,
        {"carta_sin_gluten", "cocina_separada", "acreditado"},
    ),
    (
        "Dulce Celíaca",
        "Carrer de Verdi 12",
        "08012",
        "Barcelona",
        "Barcelona",
        41.4035,
        2.1570,
        Categoria.CAFETERIA,
        {"todo_sin_gluten"},
    ),
    (
        "Mesón La Loma",
        "Calle Real 22",
        "23400",
        "Úbeda",
        "Jaén",
        38.0107,
        -3.3700,
        Categoria.RESTAURANTE,
        {"carta_sin_gluten"},
    ),
    (
        "Heladería Polo Norte",
        "Calle de Colón 15",
        "46004",
        "Valencia",
        "Valencia",
        39.4690,
        -0.3720,
        Categoria.HELADERIA,
        {"carta_sin_gluten", "acreditado"},
    ),
]


def _semana(*tramos: tuple[range | list[int], list[tuple[str, str]]]) -> list[list[dict]]:
    """Horario semanal a partir de (días, franjas); 0 es el lunes y 6 el domingo."""
    dias = [[] for _ in range(7)]
    for numeros, franjas in tramos:
        for n in numeros:
            dias[n] = [{"abre": abre, "cierra": cierra} for abre, cierra in franjas]
    return dias


TODOS = range(7)
COMIDA_Y_CENA = [("13:00", "16:30"), ("20:30", "23:30")]

# precio (1 económico, 2 normal, 3 caro), horario y nota del horario de cada sitio
INFO_PRACTICA = {
    "Celiaco Feliz": (2, _semana((range(1, 7), COMIDA_Y_CENA)), None),
    "Horno Sin Trigo": (1, _semana((range(6), [("08:00", "14:00"), ("17:30", "20:30")])), None),
    "La Pizzería Libre": (2, _semana((TODOS, [("13:00", "16:00"), ("20:00", "00:00")])), None),
    "Taberna El Arroz": (2, _semana(([0, 2, 3, 4, 5, 6], [("12:30", "17:00")])), None),
    "Burger Zero": (2, _semana((TODOS, [("13:00", "23:30")])), None),
    "Dulce Celíaca": (1, _semana((range(5), [("08:30", "20:00")]), ([5], [("09:00", "14:00")])), None),
    "Mesón La Loma": (3, _semana((range(3, 7), [("13:30", "16:30"), ("21:00", "23:30")])), None),
    "Heladería Polo Norte": (1, _semana((TODOS, [("16:00", "00:00")])), "Abre de marzo a octubre."),
}

OPINIONES = [
    # puntuación, texto
    (5, "Carta sin gluten muy completa y el personal sabe perfectamente lo que es la contaminación cruzada."),
    (5, "Las croquetas sin gluten están de escándalo. Volveré seguro."),
    (4, "Freidora separada para lo sin gluten. Por fin puedo pedir patatas tranquila."),
    (3, "Buen sitio, aunque los fines de semana hay que reservar y el servicio va lento."),
    (5, "Tienen pan sin gluten de verdad, no el típico industrial. ¡Un 10!"),
    (4, "Postres caseros aptos. La tarta de queso es imprescindible."),
    (2, "Me tuvieron que preguntar en cocina si los platos eran aptos. Les falta formación."),
    (4, "Me explicaron cada plato con detalle. Muy recomendable para celíacos."),
]

MENSAJES = [
    ("lucia", "¡Hola a todos! ¿Alguien conoce algún sitio sin gluten en Córdoba?"),
    ("marcos", "En Córdoba no, pero en Granada el Horno Sin Trigo está genial."),
    ("sara", "Confirmo lo del Horno Sin Trigo, sus magdalenas son lo mejor 😋"),
    ("javi", "Este finde voy a Málaga, apunto La Pizzería Libre."),
]


# Fotos con licencia libre de Wikimedia Commons (autoría y licencia en creditos.json)
FOTOS_DEMO = Path(__file__).resolve().parent / "demo_fotos"


def _fotos_demo(establecimiento_id: int, nombre: str, autores: list[Usuario]) -> list[Imagen]:
    creditos = json.loads((FOTOS_DEMO / "creditos.json").read_text(encoding="utf-8"))
    carpeta = get_settings().uploads_dir
    carpeta.mkdir(parents=True, exist_ok=True)
    fotos = []
    for foto, autor in zip(creditos.get(nombre, []), autores, strict=False):
        fichero = f"{uuid.uuid4().hex}.webp"
        shutil.copyfile(FOTOS_DEMO / foto["fichero"], carpeta / fichero)
        fotos.append(
            Imagen(
                establecimiento_id=establecimiento_id,
                autor_id=autor.id,
                fichero=fichero,
                credito=foto["credito"],
                credito_url=foto["credito_url"],
            )
        )
    return fotos


def _dni(numero: int) -> str:
    return f"{numero:08d}{LETRAS_DNI[numero % 23]}"


def cargar_demo() -> None:
    aleatorio = random.Random(42)
    with SessionLocal() as db:
        if db.scalar(select(Usuario.id).limit(1)):
            raise SystemExit("La base de datos ya tiene datos; no se carga la demo.")

        usuarios = {}
        for i, (username, nombre, apellidos, password, rol) in enumerate(USUARIOS):
            usuarios[username] = Usuario(
                username=username,
                email=f"{username}@example.com",
                dni=_dni(10000000 + i),
                password_hash=hash_password(password),
                nombre=nombre,
                apellidos=apellidos,
                fecha_nacimiento=date(1985 + i * 3, 1 + i, 10 + i),
                telefono=f"60000000{i}",
                rol=rol,
                creado_en=ahora() - timedelta(days=60 - i * 5),
            )
        db.add_all(usuarios.values())
        db.flush()
        miembros = [u for u in usuarios.values() if u.username != "admin"]
        actividades = []  # se insertan al final en orden cronológico (el feed se ordena por id)

        def hace(dias: float):
            return ahora() - timedelta(days=dias, minutes=aleatorio.randint(0, 600))

        for n, fila in enumerate(ESTABLECIMIENTOS):
            nombre, calle, cp, localidad, provincia, lat, lon, categoria, distintivos = fila
            autor = aleatorio.choice(list(usuarios.values()))
            est = Establecimiento(
                nombre=nombre,
                telefono=f"9530000{n:02d}",
                calle=calle,
                cod_postal=cp,
                localidad=localidad,
                provincia=provincia,
                latitud=lat,
                longitud=lon,
                creado_por_id=autor.id,
                creado_en=hace(40 - n * 3),
                categoria=categoria,
                precio=INFO_PRACTICA[nombre][0],
                horario=INFO_PRACTICA[nombre][1],
                nota_horario=INFO_PRACTICA[nombre][2],
                **dict.fromkeys(distintivos, True),
            )
            db.add(est)
            db.flush()
            # Las suben distintos miembros (sin usar `aleatorio`, para no alterar el resto de la demo)
            db.add_all(
                _fotos_demo(est.id, nombre, miembros[n % len(miembros) :] + miembros[: n % len(miembros)])
            )
            actividades.append(
                Actividad(
                    usuario_id=autor.id,
                    establecimiento_id=est.id,
                    tipo=TipoActividad.PUBLICACION,
                    creado_en=est.creado_en,
                )
            )

            for usuario in aleatorio.sample(miembros, aleatorio.randint(1, len(miembros))):
                cuando = hace(aleatorio.uniform(0, 30))
                fila = {"usuario_id": usuario.id, "establecimiento_id": est.id, "creado_en": cuando}
                db.execute(insert(visitas).values(**fila))
                actividades.append(
                    Actividad(
                        usuario_id=usuario.id,
                        establecimiento_id=est.id,
                        tipo=TipoActividad.VISITA,
                        creado_en=cuando,
                    )
                )
                if aleatorio.random() < 0.7:
                    db.execute(insert(likes).values(**fila))
                    actividades.append(
                        Actividad(
                            usuario_id=usuario.id,
                            establecimiento_id=est.id,
                            tipo=TipoActividad.LIKE,
                            creado_en=cuando,
                        )
                    )
                if aleatorio.random() < 0.4:
                    db.execute(insert(favoritos).values(**fila))
                if aleatorio.random() < 0.6:
                    puntuacion, texto = aleatorio.choice(OPINIONES)
                    db.add(
                        Comentario(
                            establecimiento_id=est.id,
                            autor_id=usuario.id,
                            puntuacion=puntuacion,
                            mensaje=texto,
                            creado_en=cuando,
                        )
                    )
                    actividades.append(
                        Actividad(
                            usuario_id=usuario.id,
                            establecimiento_id=est.id,
                            tipo=TipoActividad.COMENTARIO,
                            creado_en=cuando,
                        )
                    )

        for seguidor in miembros:
            for seguido in aleatorio.sample(miembros, 2):
                if seguido is not seguidor:
                    db.execute(insert(seguimientos).values(seguidor_id=seguidor.id, seguido_id=seguido.id))
                    db.add(
                        Notificacion(
                            usuario_id=seguido.id,
                            actor_id=seguidor.id,
                            tipo=TipoNotificacion.SEGUIDOR,
                            leida=aleatorio.random() < 0.5,
                            creado_en=hace(aleatorio.uniform(0, 20)),
                        )
                    )

        # Algunas opiniones le han resultado útiles a otras personas
        db.flush()
        for comentario in db.scalars(select(Comentario)):
            otros = [m for m in miembros if m.id != comentario.autor_id]
            for usuario in aleatorio.sample(otros, aleatorio.randint(0, 2)):
                db.execute(insert(utiles).values(usuario_id=usuario.id, comentario_id=comentario.id))
                db.add(
                    Notificacion(
                        usuario_id=comentario.autor_id,
                        actor_id=usuario.id,
                        tipo=TipoNotificacion.UTIL,
                        establecimiento_id=comentario.establecimiento_id,
                        leida=aleatorio.random() < 0.5,
                        creado_en=hace(aleatorio.uniform(0, 10)),
                    )
                )

        db.add_all(sorted(actividades, key=lambda a: a.creado_en))

        # Un aviso sobre un sitio y una consulta del formulario, para ver la bandeja del admin.
        taberna = db.scalar(select(Establecimiento).where(Establecimiento.nombre == "Taberna El Arroz"))
        db.add_all(
            [
                Consulta(
                    nombre="javi",
                    email="javi@example.com",
                    mensaje="Han cambiado de teléfono, ahora es el 954 111 222.",
                    motivo=MotivoAviso.DATOS_INCORRECTOS,
                    establecimiento_id=taberna.id,
                    creado_en=ahora() - timedelta(hours=5),
                ),
                Consulta(
                    nombre="Pepa",
                    email="pepa@example.com",
                    mensaje="¿Vais a añadir sitios en Córdoba? Me encantaría colaborar.",
                    creado_en=ahora() - timedelta(days=1),
                ),
            ]
        )

        for i, (username, texto) in enumerate(MENSAJES):
            db.add(
                Mensaje(
                    autor_id=usuarios[username].id,
                    texto=texto,
                    creado_en=ahora() - timedelta(minutes=90 - i * 12),
                )
            )
        db.commit()

    print("Demo cargada. Cuentas (email / contraseña):")
    for username, *_, password, rol in USUARIOS:
        print(f"  {username}@example.com / {password}{'  (admin)' if rol == Rol.ADMIN else ''}")


def main() -> None:
    parser = argparse.ArgumentParser(prog="python -m app.cli")
    sub = parser.add_subparsers(dest="comando", required=True)
    sub.add_parser("admin", help="Convierte una cuenta en administradora").add_argument("email")
    sub.add_parser("demo", help="Carga datos de ejemplo")
    sub.add_parser("migrar", help="Actualiza el esquema de la base de datos")
    args = parser.parse_args()

    migrar()
    if args.comando == "admin":
        hacer_admin(args.email)
    elif args.comando == "demo":
        cargar_demo()


if __name__ == "__main__":
    main()
