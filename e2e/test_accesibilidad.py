"""Auditoría automática de accesibilidad (WCAG 2.1 AA) con axe-core en las páginas principales."""

from pathlib import Path

import pytest

from conftest import entrar

AXE = Path(__file__).resolve().parent.parent / "frontend" / "node_modules" / "axe-core" / "axe.min.js"

PUBLICAS = [
    "/",
    "/establecimientos",
    "/establecimientos/1",
    "/actividad",
    "/entrar",
    "/registro",
    "/u/lucia",
    "/u/lucia?tab=opiniones",
]
PRIVADAS = ["/comunidad", "/chat", "/ajustes", "/establecimientos/nuevo", "/consultas"]
MOVIL = ["/", "/establecimientos", "/establecimientos/1", "/u/lucia", "/chat"]


def auditar(page, ruta: str) -> list[str]:
    page.goto(ruta)
    page.wait_for_load_state("networkidle")
    page.add_script_tag(path=str(AXE))
    resultado = page.evaluate(
        """() => axe.run(document, {
            runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21aa'] },
            exclude: [['.leaflet-container']],  // el mapa es de una librería externa
        })"""
    )
    return [
        f"{ruta} [{v['impact']}] {v['id']}: {v['help']} -> {v['nodes'][0]['target']}"
        for v in resultado["violations"]
        if v["impact"] in ("serious", "critical")
    ]


def _auditor(navegador, url_base, ancho: int, alto: int):
    """La Content Security Policy de la app bloquea (con razón) el script de axe que inyecta la
    auditoría, así que solo estas pruebas la desactivan en su navegador."""
    if not AXE.exists():
        pytest.skip("Falta axe-core: cd frontend && npm install")
    return navegador.new_context(
        base_url=url_base, viewport={"width": ancho, "height": alto}, bypass_csp=True
    )


@pytest.fixture
def auditor(navegador, url_base):
    contexto = _auditor(navegador, url_base, 1280, 860)
    yield contexto.new_page()
    contexto.close()


@pytest.fixture
def auditor_movil(navegador, url_base):
    contexto = _auditor(navegador, url_base, 390, 844)
    yield contexto.new_page()
    contexto.close()


def _comprobar(page, rutas):
    problemas = [p for ruta in rutas for p in auditar(page, ruta)]
    assert not problemas, "\n".join(problemas)


def test_paginas_publicas_accesibles(auditor):
    _comprobar(auditor, PUBLICAS)


def test_paginas_privadas_accesibles(auditor):
    entrar(auditor, "admin@example.com", "Admin1234")
    _comprobar(auditor, PRIVADAS)


def test_paginas_accesibles_en_movil(auditor_movil):
    entrar(auditor_movil, "lucia@example.com", "Lucia1234")
    _comprobar(auditor_movil, MOVIL)
