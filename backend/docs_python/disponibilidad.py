"""Módulo Disponibilidad — referencia mkdocstrings.

Documenta el contrato del módulo Java ``com.piedraazul.disponibilidad``
(ventana de agendamiento, periodos de disponibilidad y cálculo de franjas).
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Final


TABLAS_DISPONIBILIDAD: Final[tuple[str, ...]] = (
    "configuracion_sistema",
    "periodos_disponibilidad",
    "periodo_dias_atencion",
)

VENTANA_MINIMA_SEMANAS: Final[int] = 1
"""Mínimo de semanas que puede tener la ventana de agendamiento (RF3)."""

VENTANA_MAXIMA_SEMANAS: Final[int] = 52
"""Máximo de semanas que puede tener la ventana de agendamiento (RF3)."""

DURACION_CITA_MINIMA_MINUTOS: Final[int] = 30
"""Una cita nunca dura menos de esto; lo protege también ``TimeRange``."""

DESCANSO_MAXIMO_MINUTOS: Final[int] = 120
"""Tope del intervalo que un médico deja entre una cita y la siguiente (RF3)."""

PUERTOS_PUBLICOS: Final[dict[str, str]] = {
    "ConsultarConfiguracionPort": "Consumido por Citas: ventana y horario vigente",
    "PeriodoDisponibilidadRef": "Vista de sólo lectura del horario, para que Citas no importe la entidad",
}

PUERTOS_CONSUMIDOS: Final[dict[str, str]] = {
    "CatalogoMedicosPort": "De Personas: valida que el médico exista y esté activo",
    "ConsultarCitasPort": "De Citas: horas ya ocupadas al calcular franjas libres",
}

ENDPOINTS: Final[tuple[str, ...]] = (
    "GET /api/disponibilidad/configuracion",
    "PUT /api/disponibilidad/configuracion",
    "POST /api/disponibilidad/periodos",
    "GET /api/disponibilidad/periodos?medicoId=",
    "GET /api/disponibilidad/slots?medicoId=&fecha=",
    "GET /api/disponibilidad/slots/rango?medicoId=&desde=&hasta=",
)

CODIGOS_ERROR: Final[dict[str, int]] = {
    "VENTANA_AGENDAMIENTO_INVALIDA": 400,
    "DURACION_CITA_INVALIDA": 400,
    "DESCANSO_ENTRE_CITAS_INVALIDO": 400,
    "PERIODO_SIN_DIAS_ATENCION": 400,
    "PERIODO_RANGO_FECHAS_INVALIDO": 400,
    "PERIODO_SOLAPADO": 409,
    "MEDICO_NO_DISPONIBLE": 409,
    "FUERA_DE_VENTANA_AGENDAMIENTO": 409,
}


@dataclass(frozen=True)
class PeriodoDisponibilidadDoc:
    """Contrato de horario de un médico, válido sólo en un rango de fechas.

    Attributes:
        medico_id: Médico al que pertenece el horario.
        fecha_inicio: Primer día en que rige.
        fecha_fin: Último día en que rige; ``None`` significa "vigente hasta que
            se registre un horario nuevo".
        dias_atencion: Días de la semana en que atiende.
        hora_inicio: Comienzo de la franja horaria diaria.
        hora_fin: Fin de la franja horaria diaria.
        duracion_cita_minutos: Cuánto dura cada cita.
        descanso_entre_citas_minutos: Intervalo que el médico deja entre citas.
    """

    medico_id: int
    fecha_inicio: str
    fecha_fin: str | None
    dias_atencion: tuple[str, ...]
    hora_inicio: str
    hora_fin: str
    duracion_cita_minutos: int
    descanso_entre_citas_minutos: int

    @property
    def paso_minutos(self) -> int:
        """Paso de la rejilla de franjas: duración de la cita más el descanso."""
        return self.duracion_cita_minutos + self.descanso_entre_citas_minutos


def citas_por_dia(periodo: PeriodoDisponibilidadDoc) -> int:
    """Cuántas franjas ofrece el periodo en un día de atención.

    Reproduce la aritmética de ``CalculadorSlotsPorIntervaloFijo``: se recorre la
    franja en pasos de ``paso_minutos`` y se cuentan los que caben completos.

    Args:
        periodo: Horario configurado del médico.

    Returns:
        Número de franjas disponibles, 0 si la franja no alcanza para una cita.

    Examples:
        >>> p = PeriodoDisponibilidadDoc(1, "2026-09-27", None, ("LUNES",),
        ...                              "14:00", "18:00", 45, 15)
        >>> citas_por_dia(p)
        4
    """
    def minutos(hora: str) -> int:
        h, m = hora.split(":")
        return int(h) * 60 + int(m)

    disponible = minutos(periodo.hora_fin) - minutos(periodo.hora_inicio)
    if disponible < periodo.duracion_cita_minutos:
        return 0
    return (disponible - periodo.duracion_cita_minutos) // periodo.paso_minutos + 1


def resumen_disponibilidad() -> dict[str, object]:
    """Resumen del módulo Disponibilidad para la documentación generada.

    Returns:
        Diccionario con tablas, puertos, endpoints y códigos de error.
    """
    return {
        "modulo": "disponibilidad",
        "diagrama": "04_modulo_disponibilidad.puml",
        "tablas": TABLAS_DISPONIBILIDAD,
        "puertos_publicos": PUERTOS_PUBLICOS,
        "puertos_consumidos": PUERTOS_CONSUMIDOS,
        "endpoints": ENDPOINTS,
        "codigos_error": CODIGOS_ERROR,
        "depende_de": ["nucleo", "personas", "citas (sólo el puerto público)"],
    }
