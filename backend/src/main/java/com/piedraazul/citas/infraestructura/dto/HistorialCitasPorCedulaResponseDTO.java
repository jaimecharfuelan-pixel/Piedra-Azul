package com.piedraazul.citas.infraestructura.dto;

import java.util.List;

/**
 * Historial de citas de un paciente localizado por cédula.
 */
public record HistorialCitasPorCedulaResponseDTO(
        String cedula,
        Long pacienteId,
        String pacienteNombre,
        int cantidad,
        List<CitaResponseDTO> citas
) {
}
