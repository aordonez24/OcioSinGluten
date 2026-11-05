from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    """Configuración leída de variables de entorno o del fichero `.env`."""

    model_config = SettingsConfigDict(env_file=BASE_DIR / ".env", extra="ignore")

    # SQLite por defecto (cero configuración). Para PostgreSQL:
    # DATABASE_URL=postgresql+psycopg://usuario:clave@localhost/ociosingluten
    database_url: str = f"sqlite:///{BASE_DIR / 'ociosingluten.db'}"

    # ¡Cámbiala en producción! (python -c "import secrets; print(secrets.token_urlsafe(48))")
    secret_key: str = "cambia-esta-clave-en-produccion-por-una-larga-y-aleatoria"
    token_expira_minutos: int = 60 * 24 * 7

    uploads_dir: Path = BASE_DIR / "uploads"
    max_imagen_mb: int = 8
    max_lado_imagen_px: int = 1600

    # Orígenes permitidos para CORS (solo hace falta si el front se sirve desde otro dominio)
    cors_origins: list[str] = ["http://localhost:5173"]

    # Geocodificación de direcciones con OpenStreetMap (Nominatim).
    geocodificar: bool = True

    # Carpeta con el build del frontend. Si existe, FastAPI lo sirve directamente.
    frontend_dist: Path = BASE_DIR.parent / "frontend" / "dist"


@lru_cache
def get_settings() -> Settings:
    return Settings()
