# Imagen única: compila el frontend y lo sirve junto con la API desde FastAPI.
#   docker build -t ociosingluten .
#   docker run -p 8000:8000 -e SECRET_KEY=... -v osg-datos:/datos ociosingluten

# ---------- 1. Frontend
FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

# ---------- 2. Backend
FROM python:3.13-slim
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    DATABASE_URL=sqlite:////datos/ociosingluten.db \
    UPLOADS_DIR=/datos/uploads \
    FRONTEND_DIST=/app/frontend/dist

WORKDIR /app/backend
COPY backend/requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

COPY backend/app ./app
COPY --from=frontend /frontend/dist /app/frontend/dist

RUN useradd --create-home app && mkdir -p /datos && chown app /datos
USER app
VOLUME /datos
EXPOSE 8000

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--proxy-headers"]
