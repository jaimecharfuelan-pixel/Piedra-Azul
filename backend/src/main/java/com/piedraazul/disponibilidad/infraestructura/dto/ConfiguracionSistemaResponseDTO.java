package com.piedraazul.disponibilidad.infraestructura.dto;

import java.time.LocalDate;

/**
 * Configuración global de agendamiento (RF3).
 *
 * <p>Se devuelven además los límites ya calculados para que el formulario de
 * Angular pueda poner {@code min}/{@code max} en el selector de fecha y evitar
 * que el paciente elija un día fuera de la ventana.</p>
 */
public record ConfiguracionSistemaResponseDTO(
        Long id,
        int ventanaSemanas,
        LocalDate agendamientoDesde,
        LocalDate agendamientoHasta
) {
}
