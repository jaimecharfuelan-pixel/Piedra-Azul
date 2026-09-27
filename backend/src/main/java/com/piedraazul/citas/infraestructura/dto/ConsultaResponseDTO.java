package com.piedraazul.citas.infraestructura.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entrada del historial: lo que quedó después de atender una cita.
 */
public record ConsultaResponseDTO(
        Long id,
        Long citaId,
        Long medicoId,
        String medicoNombre,
        Long pacienteId,
        String pacienteNombre,
        LocalDate fecha,
        String observaciones,
        LocalDateTime fechaRegistro
) {
}
