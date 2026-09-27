package com.piedraazul.disponibilidad.infraestructura.dto;

import com.piedraazul.disponibilidad.dominio.EstadoVigenciaPeriodo;
import com.piedraazul.nucleo.dominio.DiaSemana;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Horario de atención de un médico tal como se muestra en la pantalla de
 * configuración (RF3).
 */
public record PeriodoDisponibilidadResponseDTO(
        Long id,
        Long medicoId,
        String medicoNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<DiaSemana> diasAtencion,
        LocalTime horaInicio,
        LocalTime horaFin,
        int duracionCitaMinutos,
        int descansoEntreCitasMinutos,
        boolean vigente,
        EstadoVigenciaPeriodo estado
) {
}
