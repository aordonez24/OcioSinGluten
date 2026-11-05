import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.responses import FileResponse, JSONResponse
from fastapi.staticfiles import StaticFiles

from app.config import Settings, get_settings
from app.db import Base, engine
from app.routers import actividades, auth, chat, consultas, establecimientos, estadisticas, usuarios


@asynccontextmanager
async def lifespan(_: FastAPI):
    if get_settings().secret_key == Settings.model_fields["secret_key"].default:
        logging.getLogger("uvicorn").warning(
            "Usando la SECRET_KEY por defecto: cualquiera podría falsificar sesiones. "
            "Defínela en backend/.env antes de publicar la aplicación."
        )
    Base.metadata.create_all(engine)
    get_settings().uploads_dir.mkdir(parents=True, exist_ok=True)
    yield


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
        if request.url.path.startswith("/assets/") or request.url.path.startswith("/uploads/"):
            # Los ficheros tienen nombre único (hash/uuid): se pueden cachear para siempre.
            respuesta.headers.setdefault("Cache-Control", "public, max-age=31536000, immutable")
        return respuesta

    for modulo in (auth, usuarios, establecimientos, actividades, estadisticas, consultas, chat):
        app.include_router(modulo.router)
    app.include_router(establecimientos.comentarios_router)

    ajustes.uploads_dir.mkdir(parents=True, exist_ok=True)
    app.mount("/uploads", StaticFiles(directory=ajustes.uploads_dir), name="uploads")

    @app.exception_handler(404)
    async def no_encontrado(request: Request, exc):
        # Las rutas del frontend (SPA) devuelven index.html; las de la API, un 404 en JSON.
        indice = ajustes.frontend_dist / "index.html"
        if not request.url.path.startswith(("/api", "/uploads")) and indice.exists():
            return FileResponse(indice)
        return JSONResponse({"detail": getattr(exc, "detail", "No encontrado")}, status_code=404)

    # En producción el frontend compilado se sirve desde aquí: un único proceso para todo.
    if ajustes.frontend_dist.exists():
        app.mount("/", StaticFiles(directory=ajustes.frontend_dist, html=True), name="frontend")

    return app


app = crear_app()
