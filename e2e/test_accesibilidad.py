"""Auditoría automática de accesibilidad (WCAG 2.1 AA) con axe-core en las páginas principales."""

from pathlib import Path

import pytest
from conftest import entrar

AXE = Path(__file__).resolve().parent.parent / "frontend" / "node_modules" / "axe-core" / "axe.min.js"

PUBLICAS = ["/", "/establecimientos", "/establecimientos/1", "/actividad", "/entrar", "/registro", "/u/lucia"]
PRIVADAS = ["/comunidad", "/chat", "/ajustes", "/establecimientos/nuevo"]


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
        f"{ruta} [{v['impact']}] {v['id']}: {v['help']} → {v['nodes'][0]['target']}"
        for v in resultado["violations"]
        if v["impact"] in ("serious", "critical")
    ]


@pytest.fixture(autouse=True)
def _axe_disponible():
    if not AXE.exists():
        pytest.skip("Falta axe-core: cd frontend && npm install")


def test_paginas_publicas_accesibles(pagina):
    problemas = [p for ruta in PUBLICAS for p in auditar(pagina, ruta)]
    assert not problemas, "\n".join(problemas)


def test_paginas_privadas_accesibles(pagina):
    entrar(pagina, "admin@example.com", "Admin1234")
    problemas = [p for ruta in [*PRIVADAS, "/consultas"] for p in auditar(pagina, ruta)]
    assert not problemas, "\n".join(problemas)
