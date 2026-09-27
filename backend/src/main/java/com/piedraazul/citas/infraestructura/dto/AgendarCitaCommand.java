package com.piedraazul.citas.infraestructura.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Datos con los que un paciente (o el agendador en su nombre) reserva una cita (RF2).
 */
public record AgendarCitaCommand(

        @NotNull(message = "Selecciona un paciente")
        Long pacienteId,

        @NotNull(message = "Selecciona un médico")
        Long medicoId,

        @NotNull(message = "Selecciona la fecha de la cita")
        LocalDate fecha,

        @NotNull(message = "Selecciona la hora de inicio")
        LocalTime horaInicio,

        @NotNull(message = "Selecciona la hora de fin")
        LocalTime horaFin
) {
}
