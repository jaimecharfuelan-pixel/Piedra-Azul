package com.piedraazul.personas.infraestructura.dto;

/**
 * Datos para crear Médico o Paciente desde Identidad (RegistrarPersonaPort).
 */
public record DatosPersonaDTO(
        Long usuarioId,
        String nombreCompleto,
        String telefono,
        Long especialidadId
) {
}
