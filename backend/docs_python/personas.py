"""Módulo Personas — referencia mkdocstrings.

Documenta el contrato del módulo Java ``com.piedraazul.personas``
(Especialidad, Médico, Paciente y puertos públicos).
"""

from __future__ import annotations

from typing import Final


TABLAS_PERSONAS: Final[tuple[str, ...]] = (
    "especialidades",
    "medicos",
    "pacientes",
)

PUERTOS_PUBLICOS: Final[dict[str, str]] = {
    "CatalogoMedicosPort": "Consumido por Disponibilidad y Citas",
    "RegistrarPersonaPort": "Consumido por Identidad (MEDICO/PACIENTE)",
}

ENDPOINTS: Final[tuple[str, ...]] = (
    "POST /api/personas/especialidades",
    "GET /api/personas/especialidades",
    "POST /api/personas/medicos",
    "GET /api/personas/medicos",
    "GET /api/personas/pacientes",
)


def resumen_personas() -> dict[str, object]:
    """Resumen del módulo Personas para la documentación generada.

    Returns:
        Diccionario con tablas, puertos y endpoints.
    """
    return {
        "modulo": "personas",
        "diagrama": "03_modulo_personas.puml",
        "tablas": TABLAS_PERSONAS,
        "puertos_publicos": PUERTOS_PUBLICOS,
        "endpoints": ENDPOINTS,
        "depende_de": ["nucleo"],
    }
