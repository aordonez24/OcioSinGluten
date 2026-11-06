"""Lo que ven WhatsApp, Telegram o los buscadores al abrir un enlace de la aplicación.

El frontend es una SPA: sin esto, todos los enlaces compartidos mostrarían la misma vista previa
genérica. Al servir index.html se rellenan el título, la descripción y la foto de la página pedida
(Open Graph), y se generan robots.txt y sitemap.xml.
"""

import re
from dataclasses import dataclass
from html import escape
from xml.sax.saxutils import escape as escape_xml

from fastapi import Request
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.config import get_settings
from app.models import Establecimiento, Usuario

NOMBRE = "Ocio Sin Gluten"
TITULO_INICIO = "Ocio Sin Gluten - Disfrute de su tiempo de ocio sin gluten"
DESCRIPCION_INICIO = "Comunidad para encontrar sitios de ocio y restauración sin gluten."
ICONO = "/iconos/icono-512.png"

CATEGORIAS = {
    "restaurante": "Restaurante",
    "cafeteria": "Cafetería",
    "panaderia": "Panadería y pastelería",
    "bar": "Bar",
    "heladeria": "Heladería",
    "otro": "Sitio",
}
DISTINTIVOS = {
    "carta_sin_gluten": "carta sin gluten",
    "cocina_separada": "cocina separada",
    "acreditado": "acreditado por una asociación de celíacos",
}

# Páginas privadas o que solo tienen sentido con sesión. Los perfiles y la actividad tampoco se
# indexan: dicen quién es celíaco, y eso es un dato de salud.
NO_INDEXAR = (
    "/api/",
    "/u/",
    "/actividad",
    "/ajustes",
    "/chat",
    "/comunidad",
    "/consultas",
    "/entrar",
    "/registro",
    "/recuperar",
    "/restablecer",
)


@dataclass
class Metadatos:
    titulo: str = TITULO_INICIO
    descripcion: str = DESCRIPCION_INICIO
    imagen: str = ICONO
    imagen_grande: bool = False  # foto apaisada (tarjeta grande) o el icono cuadrado
    estado: int = 200


def url_base(request: Request) -> str:
    """Dirección pública de la aplicación, para los enlaces absolutos (correos, vistas previas)."""
    return (get_settings().url_publica or str(request.base_url)).rstrip("/")


def _numero(valor: float) -> str:
    return f"{valor:.1f}".replace(".", ",").removesuffix(",0")


def _describir(est: Establecimiento) -> str:
    donde = est.localidad if est.localidad == est.provincia else f"{est.localidad} ({est.provincia})"
    frases = [f"{CATEGORIAS.get(est.categoria, 'Sitio')} en {donde}"]
    if est.todo_sin_gluten:
        frases.append("100 % sin gluten")
    distintivos = [texto for clave, texto in DISTINTIVOS.items() if getattr(est, clave)]
    if distintivos:
        texto = ", ".join(distintivos)
        frases.append(texto[0].upper() + texto[1:])
    if est.valoracion is not None:
        opiniones = "1 opinión" if est.num_opiniones == 1 else f"{est.num_opiniones} opiniones"
        frases.append(f"{_numero(est.valoracion)} de 5 en {opiniones}")
    return ". ".join(frases) + "."


def metadatos(db: Session, request: Request) -> Metadatos:
    ruta = request.url.path.rstrip("/") or "/"

    if m := re.fullmatch(r"/establecimientos/(\d+)", ruta):
        est = db.get(Establecimiento, int(m[1]))
        if est is None:
            return Metadatos(titulo=f"No encontrado - {NOMBRE}", estado=404)
        return Metadatos(
            titulo=f"{est.nombre} - {NOMBRE}",
            descripcion=_describir(est),
            imagen=f"/uploads/{est.portada}" if est.portada else ICONO,
            imagen_grande=bool(est.portada),
        )

    if m := re.fullmatch(r"/u/([^/]+)", ruta):
        usuario = db.scalar(select(Usuario).where(Usuario.username == m[1], Usuario.archivado.is_(False)))
        if usuario is None:
            return Metadatos(titulo=f"No encontrado - {NOMBRE}", estado=404)
        return Metadatos(
            titulo=f"{usuario.username} - {NOMBRE}",
            descripcion=f"Perfil de {usuario.nombre} en {NOMBRE}: sus opiniones y sus sitios sin gluten.",
            imagen=f"/uploads/{usuario.foto}" if usuario.foto else ICONO,
        )

    if ruta == "/establecimientos":
        provincia = request.query_params.get("provincia")
        donde = f" en {provincia}" if provincia and len(provincia) <= 60 else ""
        return Metadatos(
            titulo=f"Sitios sin gluten{donde} - {NOMBRE}",
            descripcion=f"Restaurantes, cafeterías, panaderías y bares sin gluten{donde}, con las "
            "opiniones de la comunidad celíaca.",
        )

    return Metadatos()


def pagina(db: Session, request: Request) -> tuple[str, int] | None:
    """index.html con las etiquetas de la página pedida, o None si no hay frontend compilado."""
    indice = get_settings().frontend_dist / "index.html"
    if not indice.exists():
        return None
    html = indice.read_text(encoding="utf-8")
    meta = metadatos(db, request)
    base = url_base(request)
    url = base + request.url.path

    def atributo(texto: str) -> str:
        return escape(texto, quote=True)

    tarjeta = "summary_large_image" if meta.imagen_grande else "summary"
    etiquetas = [
        f'<meta property="og:site_name" content="{NOMBRE}" />',
        '<meta property="og:locale" content="es_ES" />',
        '<meta property="og:type" content="website" />',
        f'<meta property="og:title" content="{atributo(meta.titulo.removesuffix(f" - {NOMBRE}"))}" />',
        f'<meta property="og:description" content="{atributo(meta.descripcion)}" />',
        f'<meta property="og:url" content="{atributo(url)}" />',
        f'<meta property="og:image" content="{atributo(base + meta.imagen)}" />',
        f'<meta name="twitter:card" content="{tarjeta}" />',
    ]
    if request.url.path.startswith(NO_INDEXAR) or meta.estado != 200:
        etiquetas.append('<meta name="robots" content="noindex" />')
    else:
        etiquetas.append(f'<link rel="canonical" href="{atributo(url)}" />')

    html = re.sub(r"<title>.*?</title>", lambda _: f"<title>{escape(meta.titulo)}</title>", html, count=1)
    html = re.sub(
        r'<meta name="description" content="[^"]*"\s*/?>',
        lambda _: f'<meta name="description" content="{atributo(meta.descripcion)}" />',
        html,
        count=1,
    )
    bloque = "\n    " + "\n    ".join(etiquetas) + "\n  </head>"
    html = re.sub(r"\s*</head>", lambda _: bloque, html, count=1)
    return html, meta.estado


def robots(request: Request) -> str:
    lineas = ["User-agent: *", *(f"Disallow: {ruta}" for ruta in NO_INDEXAR)]
    return "\n".join([*lineas, "", f"Sitemap: {url_base(request)}/sitemap.xml", ""])


def sitemap(db: Session, request: Request) -> str:
    base = url_base(request)
    rutas = ["/", "/establecimientos"]
    ids = db.scalars(select(Establecimiento.id).order_by(Establecimiento.id))
    rutas += [f"/establecimientos/{i}" for i in ids]
    urls = "\n".join(f"  <url><loc>{escape_xml(base + ruta)}</loc></url>" for ruta in rutas)
    return (
        '<?xml version="1.0" encoding="UTF-8"?>\n'
        '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n'
        f"{urls}\n</urlset>\n"
    )
