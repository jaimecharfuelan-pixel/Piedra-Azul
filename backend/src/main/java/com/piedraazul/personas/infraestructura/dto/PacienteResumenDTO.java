package com.piedraazul.personas.infraestructura.dto;

/**
 * Proyección mínima de un paciente para otros módulos (Citas la usa para
 * mostrar el nombre en el listado del RF1 sin exponer la entidad Paciente).
 */
public record PacienteResumenDTO(Long id, String nombreCompleto, String telefono) {
}
