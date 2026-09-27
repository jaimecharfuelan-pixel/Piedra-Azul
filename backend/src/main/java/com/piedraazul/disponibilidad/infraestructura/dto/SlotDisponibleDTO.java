package com.piedraazul.disponibilidad.infraestructura.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Franja libre que consume FullCalendar en Angular (RF2).
 */
public record SlotDisponibleDTO(
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
) {
}
