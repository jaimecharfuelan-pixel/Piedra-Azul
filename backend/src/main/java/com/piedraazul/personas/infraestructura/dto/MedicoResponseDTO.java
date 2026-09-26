package com.piedraazul.personas.infraestructura.dto;

public record MedicoResponseDTO(
        Long id,
        String nombreCompleto,
        EspecialidadResponseDTO especialidad,
        boolean activo
) {
}
