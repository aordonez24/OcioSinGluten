"""Envío de correos. Con SMTP configurado se mandan de verdad; sin él (desarrollo y pruebas) se guardan
como ficheros .eml en la carpeta `correos_dir`, que se pueden abrir con cualquier programa de correo."""

import logging
import smtplib
from email.message import EmailMessage
from uuid import uuid4

from app.config import get_settings

log = logging.getLogger("uvicorn.error")


def enviar(destino: str, asunto: str, texto: str) -> None:
    ajustes = get_settings()
    mensaje = EmailMessage()
    mensaje["From"] = ajustes.correo_remitente
    mensaje["To"] = destino
    mensaje["Subject"] = asunto
    mensaje.set_content(texto)

    if not ajustes.smtp_host:
        ajustes.correos_dir.mkdir(parents=True, exist_ok=True)
        fichero = ajustes.correos_dir / f"{uuid4().hex}.eml"
        fichero.write_bytes(bytes(mensaje))
        log.info("Correo para %s guardado en %s (no hay SMTP configurado)", destino, fichero)
        return

    try:
        with smtplib.SMTP(ajustes.smtp_host, ajustes.smtp_puerto, timeout=20) as smtp:
            if ajustes.smtp_tls:
                smtp.starttls()
            if ajustes.smtp_usuario:
                smtp.login(ajustes.smtp_usuario, ajustes.smtp_password or "")
            smtp.send_message(mensaje)
    except (OSError, smtplib.SMTPException):
        # Se envía en segundo plano: el error no puede llegar a quien lo pidió, pero queda registrado.
        log.exception("No se pudo enviar el correo a %s", destino)
