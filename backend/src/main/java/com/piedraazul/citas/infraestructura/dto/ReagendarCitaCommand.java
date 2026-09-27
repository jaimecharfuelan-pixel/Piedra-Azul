package com.piedraazul.citas.infraestructura.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Nueva fecha y hora de una cita ya programada. El médico no cambia.
 */
public record ReagendarCitaCommand(

        @NotNull(message = "Selecciona la nueva fecha")
        LocalDate fecha,

        @NotNull(message = "Selecciona la nueva hora de inicio")
        LocalTime horaInicio,

        @NotNull(message = "Selecciona la nueva hora de fin")
        LocalTime horaFin
) {
}
