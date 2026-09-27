package com.piedraazul.citas.infraestructura.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Resultado del RF1: el listado de citas de un médico en una fecha, con la
 * cantidad total y el desglose por estado para las tarjetas de resumen.
 */
public record ListadoCitasResponseDTO(
        Long medicoId,
        String medicoNombre,
        LocalDate fecha,
        String orden,
        int cantidad,
        int cantidadProgramadas,
        int cantidadCanceladas,
        int cantidadAtendidas,
        List<CitaResponseDTO> citas
) {
}
