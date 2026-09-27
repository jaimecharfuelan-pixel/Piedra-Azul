package com.piedraazul.disponibilidad.infraestructura.dto;

import com.piedraazul.nucleo.dominio.DiaSemana;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Datos con los que Admin/Agendador/Médico define el horario de atención (RF3).
 *
 * <p>{@code fechaFin} es opcional: si viene nula el periodo queda abierto y se
 * cierra automáticamente cuando se registre uno posterior.</p>
 */
public record ConfigurarPeriodoCommand(

        @NotNull(message = "Selecciona un médico")
        Long medicoId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        LocalDate fechaFin,

        @NotEmpty(message = "Selecciona al menos un día de atención")
        Set<DiaSemana> diasAtencion,

        @NotNull(message = "La hora de inicio es obligatoria")
        LocalTime horaInicio,

        @NotNull(message = "La hora de fin es obligatoria")
        LocalTime horaFin,

        @Min(value = 30, message = "La cita debe durar al menos 30 minutos")
        @Max(value = 480, message = "La cita no puede durar más de 8 horas")
        int duracionCitaMinutos,

        @Min(value = 0, message = "El descanso entre citas no puede ser negativo")
        @Max(value = 120, message = "El descanso entre citas no puede pasar de 120 minutos")
        Integer descansoEntreCitasMinutos
) {

    /** El descanso es opcional en la API: si no viene, las citas van seguidas. */
    public int descansoEnMinutos() {
        return descansoEntreCitasMinutos == null ? 0 : descansoEntreCitasMinutos;
    }
}
