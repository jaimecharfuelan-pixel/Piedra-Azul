package com.piedraazul.citas.infraestructura.dto;

import com.piedraazul.nucleo.dominio.EstadoCita;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Cita tal como se muestra en la tabla del agendador y en el calendario.
 *
 * <p>Incluye los nombres resueltos (y no sólo los ids) para que la interfaz no
 * tenga que hacer una consulta extra por fila.</p>
 */
public record CitaResponseDTO(
        Long id,
        Long medicoId,
        String medicoNombre,
        Long pacienteId,
        String pacienteNombre,
        String pacienteTelefono,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        long duracionMinutos,
        EstadoCita estado
) {
}
