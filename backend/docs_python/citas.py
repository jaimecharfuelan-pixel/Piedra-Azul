"""Módulo Citas — referencia mkdocstrings.

Documenta el contrato del módulo Java ``com.piedraazul.citas``
(ciclo de vida de la cita, listado del agendador e historial de consultas).
"""

from __future__ import annotations

from typing import Final


TABLAS_CITAS: Final[tuple[str, ...]] = ("citas", "consultas")

ESTADOS_CITA: Final[tuple[str, ...]] = ("PROGRAMADA", "CANCELADA", "ATENDIDA")
"""``CANCELADA`` y ``ATENDIDA`` son finales: no admiten más cambios."""

TRANSICIONES: Final[dict[str, tuple[str, ...]]] = {
    "PROGRAMADA": ("PROGRAMADA", "CANCELADA", "ATENDIDA"),
    "CANCELADA": (),
    "ATENDIDA": (),
}
"""Estados a los que puede pasar una cita desde cada estado.

``PROGRAMADA -> PROGRAMADA`` corresponde a reagendar: cambia fecha y hora pero no
el estado.
"""

ORDENES_LISTADO: Final[dict[str, str]] = {
    "HORA_ASC": "Base de datos, con el índice (medico_id, fecha)",
    "HORA_DESC": "Base de datos",
    "ESTADO_ASC": "Base de datos",
    "ESTADO_DESC": "Base de datos",
    "PACIENTE_ASC": "En memoria: el nombre vive en el módulo Personas",
    "PACIENTE_DESC": "En memoria",
}
"""Criterios de ordenamiento del RF1 y dónde se resuelve cada uno."""

VALIDACIONES_AGENDAR: Final[tuple[str, ...]] = (
    "paciente registrado (RF2 exige registro previo)",
    "médico existente y activo",
    "rango horario válido y de al menos 30 minutos",
    "fecha dentro de la ventana de agendamiento",
    "la franja no ha pasado todavía",
    "la hora cae dentro del horario vigente y coincide con un slot de la rejilla",
    "no se solapa con otra cita PROGRAMADA del mismo médico",
)
"""Comprobaciones que comparten ``agendar()`` y ``reagendar()``, en orden."""

INDICES: Final[dict[str, str]] = {
    "idx_cita_medico_fecha": "RF1 y vista de calendario",
    "idx_cita_medico_fecha_estado": "rangos ocupados y detección de solapamiento",
    "idx_cita_paciente": "'mis citas' del paciente",
    "idx_consulta_paciente": "historial del paciente",
    "idx_consulta_medico": "historial del médico",
}

PUERTOS_PUBLICOS: Final[dict[str, str]] = {
    "ConsultarCitasPort": "Consumido por Disponibilidad: rangos ya ocupados",
}

PUERTOS_CONSUMIDOS: Final[dict[str, str]] = {
    "CatalogoMedicosPort": "De Personas: médico activo y su nombre",
    "CatalogoPacientesPort": "De Personas: paciente registrado, nombre y teléfono",
    "ConsultarConfiguracionPort": "De Disponibilidad: ventana y horario vigente",
}

ENDPOINTS: Final[tuple[str, ...]] = (
    "POST /api/citas",
    "PUT /api/citas/{citaId}/cancelacion",
    "PUT /api/citas/{citaId}/reagendamiento",
    "PUT /api/citas/{citaId}/atencion",
    "GET /api/citas?medicoId=&fecha=&orden=",
    "GET /api/citas/rango?medicoId=&desde=&hasta=",
    "GET /api/citas/paciente/{pacienteId}",
    "GET /api/citas/historial/paciente/{pacienteId}",
    "GET /api/citas/historial/medico/{medicoId}",
)

CODIGOS_ERROR: Final[dict[str, int]] = {
    "CITA_NO_ENCONTRADA": 404,
    "PACIENTE_NO_ENCONTRADO": 404,
    "DATOS_INVALIDOS": 400,
    "ORDEN_INVALIDO": 400,
    "SLOT_NO_DISPONIBLE": 409,
    "CITA_NO_MODIFICABLE": 409,
    "MEDICO_NO_DISPONIBLE": 409,
    "FUERA_DE_VENTANA_AGENDAMIENTO": 409,
}


def puede_modificarse(estado: str) -> bool:
    """Indica si una cita en ese estado admite cancelar, reagendar o atender.

    Args:
        estado: Uno de :data:`ESTADOS_CITA`.

    Returns:
        ``True`` sólo si la cita está PROGRAMADA.

    Examples:
        >>> puede_modificarse("PROGRAMADA")
        True
        >>> puede_modificarse("ATENDIDA")
        False
    """
    return estado == "PROGRAMADA"


def resumen_citas() -> dict[str, object]:
    """Resumen del módulo Citas para la documentación generada.

    Returns:
        Diccionario con tablas, estados, órdenes, puertos y endpoints.
    """
    return {
        "modulo": "citas",
        "diagrama": "05_modulo_citas.puml",
        "tablas": TABLAS_CITAS,
        "estados": ESTADOS_CITA,
        "ordenes_listado": ORDENES_LISTADO,
        "indices": INDICES,
        "puertos_publicos": PUERTOS_PUBLICOS,
        "puertos_consumidos": PUERTOS_CONSUMIDOS,
        "endpoints": ENDPOINTS,
        "codigos_error": CODIGOS_ERROR,
        "depende_de": ["nucleo", "personas", "disponibilidad"],
    }
