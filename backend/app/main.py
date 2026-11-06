import logging
import mimetypes
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.responses import HTMLResponse, JSONResponse, PlainTextResponse, Response
from fastapi.staticfiles import StaticFiles

from app.config import Settings, get_settings
from app.db import SessionLocal, migrar
from app.routers import (
    actividades,
    auth,
    chat,
    consultas,
    establecimientos,
    estadisticas,
    notificaciones,
    usuarios,
)
from app.security import DB
from app.services import compartir, tarjeta
from app.services.consultas import establecimiento_o_404

# Para que el manifiesto de la app instalable (PWA) se sirva con su tipo correcto.
mimetypes.add_type("application/manifest+json", ".webmanifest")


@asynccontextmanager
async def lifespan(_: FastAPI):
    if get_settings().secret_key == Settings.model_fields["secret_key"].default:
        logging.getLogger("uvicorn").warning(
            "Usando la SECRET_KEY por defecto: cualquiera podría falsificar sesiones. "
            "Defínela en backend/.env antes de publicar la aplicación."
        )
    if get_settings().smtp_host and not get_settings().url_publica:
        logging.getLogger("uvicorn").warning(
            "Define URL_PUBLICA: sin ella, los enlaces de los correos usan la dirección de cada petición."
        )
    migrar()  # aplica las migraciones pendientes (Alembic)
    get_settings().uploads_dir.mkdir(parents=True, exist_ok=True)
    yield


def politica_contenido(request: Request) -> str:
    """Content Security Policy: solo se ejecuta código propio, aunque alguien consiga inyectar HTML."""
    host = request.headers.get("host", "")
    return "; ".join(
        [
            "default-src 'self'",
            "script-src 'self'",
            "style-src 'self' 'unsafe-inline'",  # estilos dinámicos de Vue y Leaflet
            "img-src 'self' data: blob: https://*.tile.openstreetmap.org",  # blob: previsualizar fotos
            f"connect-src 'self' ws://{host} wss://{host}",  # API y chat
            "font-src 'self'",
            "object-src 'none'",
            "base-uri 'self'",
            "form-action 'self'",
            "frame-ancestors 'none'",
        ]
    )


def crear_app() -> FastAPI:
    ajustes = get_settings()
    app = FastAPI(title="Ocio Sin Gluten", version="2.0", lifespan=lifespan)

    app.add_middleware(
        CORSMiddleware,
        allow_origins=ajustes.cors_origins,
        allow_methods=["*"],
        allow_headers=["*"],
    )
    app.add_middleware(GZipMiddleware, minimum_size=1024)

    @app.middleware("http")
    async def cabeceras_seguridad(request: Request, call_next):
        respuesta = await call_next(request)
        respuesta.headers.setdefault("X-Content-Type-Options", "nosniff")
        respuesta.headers.setdefault("X-Frame-Options", "DENY")
        respuesta.headers.setdefault("Referrer-Policy", "strict-origin-when-cross-origin")
        respuesta.headers.setdefault("Permissions-Policy", "camera=(), microphone=(), geolocation=(self)")
        if not request.url.path.startswith(("/docs", "/redoc")):  # Swagger UI carga recursos de un CDN
            respuesta.headers.setdefault("Content-Security-Policy", politica_contenido(request))
        if request.url.path.startswith("/assets/") or request.url.path.startswith("/uploads/"):
            # Los ficheros tienen nombre único (hash/uuid): se pueden cachear para siempre.
            respuesta.headers.setdefault("Cache-Control", "public, max-age=31536000, immutable")
        return respuesta

    routers = (auth, usuarios, establecimientos, actividades, estadisticas, consultas, notificaciones, chat)
    for modulo in routers:
        app.include_router(modulo.router)
    app.include_router(establecimientos.comentarios_router)

    ajustes.uploads_dir.mkdir(parents=True, exist_ok=True)
    app.mount("/uploads", StaticFiles(directory=ajustes.uploads_dir), name="uploads")

    def pagina_spa(request: Request, db) -> Response | None:
        """index.html con el título, la descripción y la foto de la página (vista previa al compartir)."""
        resultado = compartir.pagina(db, request)
        if resultado is None:
            return None
        html, estado = resultado
        # Sin caché: así los cambios de un sitio se ven en la vista previa y en cada versión nueva.
        return HTMLResponse(html, status_code=estado, headers={"Cache-Control": "no-cache"})

    @app.get("/", include_in_schema=False)
    def inicio(request: Request, db: DB):
        return pagina_spa(request, db) or JSONResponse({"detail": "No encontrado"}, status_code=404)

    @app.get("/robots.txt", include_in_schema=False)
    def robots(request: Request):
        return PlainTextResponse(compartir.robots(request))

    @app.get("/sitemap.xml", include_in_schema=False)
    def sitemap(request: Request, db: DB):
        return Response(compartir.sitemap(db, request), media_type="application/xml")

    @app.get("/vista-previa/{est_id}.jpg", include_in_schema=False)
    def vista_previa(est_id: int, db: DB):
        """Imagen de 1200×630 para la vista previa de un sitio al compartirlo."""
        imagen = tarjeta.dibujar(tarjeta.DatosTarjeta.de(establecimiento_o_404(db, est_id)))
        return Response(imagen, media_type="image/jpeg", headers={"Cache-Control": "public, max-age=3600"})

    @app.exception_handler(404)
    async def no_encontrado(request: Request, exc):
        # Las rutas del frontend (SPA) devuelven index.html; las de la API, un 404 en JSON.
        if not request.url.path.startswith(("/api", "/uploads")):
            with SessionLocal() as db:
                respuesta = pagina_spa(request, db)
            if respuesta is not None:
                return respuesta
        return JSONResponse({"detail": getattr(exc, "detail", "No encontrado")}, status_code=404)

    # En producción el frontend compilado se sirve desde aquí: un único proceso para todo.
    if ajustes.frontend_dist.exists():
        app.mount("/", StaticFiles(directory=ajustes.frontend_dist, html=True), name="frontend")

    return app


app = crear_app()
