package com.piedraazul.personas.infraestructura.dto;

/**
 * Resumen ligero del paciente para listados de otros módulos (p. ej. Citas).
 */
public record PacienteResumenDTO(Long id, String cedula, String nombreCompleto, String telefono) {
}
