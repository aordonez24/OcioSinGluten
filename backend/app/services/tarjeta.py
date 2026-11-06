"""Tarjeta de 1200×630 que se ve al compartir un sitio en WhatsApp, Telegram o redes sociales.

Foto del sitio a la izquierda y, a la derecha, su nombre, la valoración y los distintivos con los
colores y la letra (Montserrat) de la aplicación. Se dibuja con Pillow al pedirla y se guarda en
memoria: solo cambia cuando cambian esos datos.
"""

from dataclasses import dataclass
from functools import lru_cache
from io import BytesIO
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

from app.config import get_settings
from app.models import Establecimiento

RECURSOS = Path(__file__).resolve().parent.parent / "recursos"
ANCHO, ALTO = 1200, 630
ANCHO_FOTO = 540
MARGEN = 56

BLANCO = (255, 255, 255)
TURQUESA = (157, 217, 210)
CARBON = (53, 53, 53)
TEXTO_SUAVE = (91, 102, 100)
ESTRELLA = (227, 154, 11)
ESTRELLA_VACIA = (211, 225, 223)
VERDE, VERDE_CLARO = (29, 106, 58), (226, 242, 231)
NIEBLA = (239, 246, 245)

CATEGORIAS = {
    "restaurante": "Restaurante",
    "cafeteria": "Cafetería",
    "panaderia": "Panadería y pastelería",
    "bar": "Bar",
    "heladeria": "Heladería",
    "otro": "Sitio",
}


@dataclass(frozen=True)
class DatosTarjeta:
    nombre: str
    categoria: str
    localidad: str
    valoracion: float | None
    num_opiniones: int
    portada: str | None
    distintivos: tuple[str, ...]

    @classmethod
    def de(cls, est: Establecimiento) -> "DatosTarjeta":
        distintivos = [
            texto
            for activo, texto in (
                (est.todo_sin_gluten, "100 % sin gluten"),
                (est.carta_sin_gluten, "Carta sin gluten"),
                (est.cocina_separada, "Cocina separada"),
                (est.acreditado, "Acreditado"),
            )
            if activo
        ]
        return cls(
            nombre=est.nombre,
            categoria=CATEGORIAS.get(est.categoria, "Sitio"),
            localidad=est.localidad,
            valoracion=est.valoracion,
            num_opiniones=est.num_opiniones,
            portada=est.portada,
            distintivos=tuple(distintivos),
        )


@lru_cache(maxsize=32)
def _fuente(tamano: int, peso: int) -> ImageFont.FreeTypeFont:
    fuente = ImageFont.truetype(str(RECURSOS / "montserrat.woff2"), tamano)
    fuente.set_variation_by_axes([peso])
    return fuente


def _lineas(texto: str, fuente: ImageFont.FreeTypeFont, ancho: int) -> list[str]:
    lineas, actual = [], ""
    for palabra in texto.split():
        prueba = f"{actual} {palabra}".strip()
        if fuente.getlength(prueba) <= ancho or not actual:
            actual = prueba
        else:
            lineas.append(actual)
            actual = palabra
    return [*lineas, actual]


def _estrella(dibujo: ImageDraw.ImageDraw, x: float, y: float, r: float, color) -> None:
    from math import cos, pi, sin

    puntos = [
        (x + (r if i % 2 == 0 else r * 0.45) * cos(-pi / 2 + i * pi / 5),
         y + (r if i % 2 == 0 else r * 0.45) * sin(-pi / 2 + i * pi / 5))
        for i in range(10)
    ]  # fmt: skip
    dibujo.polygon(puntos, fill=color)


def _foto(portada: str | None) -> Image.Image:
    ruta = get_settings().uploads_dir / portada if portada else None
    if ruta and ruta.exists():
        return ImageOps.fit(Image.open(ruta).convert("RGB"), (ANCHO_FOTO, ALTO), Image.LANCZOS)
    # Sin foto: el turquesa de la marca con el logo, nada de imágenes inventadas
    panel = Image.new("RGB", (ANCHO_FOTO, ALTO), TURQUESA)
    logo = Image.open(RECURSOS / "logo.png").convert("RGB").resize((240, 240), Image.LANCZOS)
    mascara = Image.new("L", logo.size, 0)
    ImageDraw.Draw(mascara).ellipse((0, 0, 239, 239), fill=255)
    panel.paste(logo, ((ANCHO_FOTO - 240) // 2, (ALTO - 240) // 2), mascara)
    return panel


@lru_cache(maxsize=64)
def dibujar(datos: DatosTarjeta) -> bytes:
    tarjeta = Image.new("RGB", (ANCHO, ALTO), BLANCO)
    tarjeta.paste(_foto(datos.portada), (0, 0))
    dibujo = ImageDraw.Draw(tarjeta)
    x = ANCHO_FOTO + MARGEN
    ancho_texto = ANCHO - x - MARGEN

    # Marca
    logo = Image.open(RECURSOS / "logo.png").convert("RGB").resize((60, 60), Image.LANCZOS)
    mascara = Image.new("L", (60, 60), 0)
    ImageDraw.Draw(mascara).ellipse((0, 0, 59, 59), fill=255)
    tarjeta.paste(logo, (x, 44), mascara)
    dibujo.text((x + 76, 74), "Ocio Sin Gluten", font=_fuente(28, 700), fill=CARBON, anchor="lm")

    # Tipo y localidad
    y = 150
    dibujo.text((x, y), f"{datos.categoria} en {datos.localidad}", font=_fuente(26, 500), fill=TEXTO_SUAVE)

    # Nombre: tan grande como quepa en dos líneas
    for tamano in (66, 58, 50, 44, 38):
        fuente = _fuente(tamano, 700)
        lineas = _lineas(datos.nombre, fuente, ancho_texto)
        if len(lineas) <= 2:
            break
    if len(lineas) > 2:  # ni con la letra más pequeña cabe: se corta con puntos suspensivos
        segunda = " ".join(lineas[1:])
        while fuente.getlength(segunda + "…") > ancho_texto:
            segunda = segunda[:-1]
        lineas = [lineas[0], segunda.rstrip() + "…"]
    y += 48
    for linea in lineas:
        dibujo.text((x, y), linea, font=fuente, fill=CARBON)
        y += round(tamano * 1.15)

    # Valoración
    y += 26
    if datos.valoracion is not None:
        for i in range(5):
            relleno = ESTRELLA if datos.valoracion >= i + 0.5 else ESTRELLA_VACIA
            _estrella(dibujo, x + 18 + i * 40, y + 18, 18, relleno)
        opiniones = "1 opinión" if datos.num_opiniones == 1 else f"{datos.num_opiniones} opiniones"
        nota = f"{datos.valoracion:.1f}".replace(".", ",")
        dibujo.text(
            (x + 214, y + 18), f"{nota} de 5, {opiniones}", font=_fuente(26, 600), fill=CARBON, anchor="lm"
        )
    else:
        dibujo.text((x, y + 18), "Aún sin opiniones", font=_fuente(26, 500), fill=TEXTO_SUAVE, anchor="lm")

    # Distintivos, como las píldoras de la ficha
    y += 66
    cx = x
    fuente = _fuente(22, 600)
    for texto in datos.distintivos:
        ancho = round(fuente.getlength(texto)) + 36
        if cx + ancho > ANCHO - MARGEN:
            break
        verde = texto.startswith("100")
        dibujo.rounded_rectangle(
            (cx, y, cx + ancho, y + 44), radius=22, fill=VERDE_CLARO if verde else NIEBLA
        )
        dibujo.text((cx + 18, y + 22), texto, font=fuente, fill=VERDE if verde else CARBON, anchor="lm")
        cx += ancho + 12

    # Pie turquesa, como el de la web
    dibujo.rectangle((ANCHO_FOTO, ALTO - 72, ANCHO, ALTO), fill=TURQUESA)
    dibujo.text(
        (x, ALTO - 36),
        "Recomendado por personas celíacas",
        font=_fuente(22, 600),
        fill=CARBON,
        anchor="lm",
    )

    salida = BytesIO()
    tarjeta.save(salida, "JPEG", quality=86, optimize=True, progressive=True)
    return salida.getvalue()
