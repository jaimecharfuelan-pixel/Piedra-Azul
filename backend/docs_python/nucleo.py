"""Núcleo común PiedraAzul — referencia para mkdocstrings.

Documenta el contrato compartido (enums, TimeRange, excepciones) que
implementa el backend Java en ``com.piedraazul.nucleo``.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Final


DURACION_MINIMA_CITA_MINUTOS: Final[int] = 30

DIAS_SEMANA: Final[tuple[str, ...]] = (
    "LUNES",
    "MARTES",
    "MIERCOLES",
    "JUEVES",
    "VIERNES",
    "SABADO",
    "DOMINGO",
)

ROLES_USUARIO: Final[tuple[str, ...]] = (
    "ADMINISTRADOR",
    "AGENDADOR",
    "MEDICO",
    "PACIENTE",
)

ESTADOS_CITA: Final[tuple[str, ...]] = (
    "PROGRAMADA",
    "CANCELADA",
    "ATENDIDA",
)


@dataclass(frozen=True)
class TimeRangeDoc:
    """Espejo documental del Value Object ``TimeRange`` de Java.

    Attributes:
        hora_inicio: Hora de inicio en formato ``HH:mm``.
        hora_fin: Hora de fin en formato ``HH:mm`` (debe ser posterior).
    """

    hora_inicio: str
    hora_fin: str

    def duracion_minutos(self) -> int:
        """Calcula la duración en minutos.

        Returns:
            Minutos entre inicio y fin.

        Raises:
            ValueError: Si la duración es menor a 30 minutos.
        """
        h0, m0 = map(int, self.hora_inicio.split(":"))
        h1, m1 = map(int, self.hora_fin.split(":"))
        minutos = (h1 * 60 + m1) - (h0 * 60 + m0)
        if minutos < DURACION_MINIMA_CITA_MINUTOS:
            raise ValueError("DURACION_INVALIDA")
        return minutos

    def solapa_con(self, otro: TimeRangeDoc) -> bool:
        """Indica si dos franjas se solapan.

        Args:
            otro: Otra franja horaria.

        Returns:
            ``True`` si hay solapamiento.
        """
        a0 = _a_minutos(self.hora_inicio)
        a1 = _a_minutos(self.hora_fin)
        b0 = _a_minutos(otro.hora_inicio)
        b1 = _a_minutos(otro.hora_fin)
        return a0 < b1 and b0 < a1


def _a_minutos(hhmm: str) -> int:
    h, m = map(int, hhmm.split(":"))
    return h * 60 + m


def elementos_nucleo() -> dict[str, object]:
    """Resumen del núcleo común para la documentación generada.

    Returns:
        Diccionario con enums, duración mínima y mapeo a RF.
    """
    return {
        "dias_semana": DIAS_SEMANA,
        "roles": ROLES_USUARIO,
        "estados_cita": ESTADOS_CITA,
        "duracion_minima_minutos": DURACION_MINIMA_CITA_MINUTOS,
        "requisitos": {
            "RF1": "RolUsuario.AGENDADOR + EstadoCita",
            "RF2": "RolUsuario.PACIENTE + TimeRange (slots)",
            "RF3": "RolUsuario.ADMINISTRADOR + DiaSemana + TimeRange",
        },
    }
