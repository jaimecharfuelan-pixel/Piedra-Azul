package com.piedraazul.personas.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Alta operativa (walk-in) o datos de ficha del paciente.
 */
public record RegistrarPacienteCommand(

        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{6,15}", message = "La cédula debe tener entre 6 y 15 dígitos")
        String cedula,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 180, message = "El nombre debe tener entre 3 y 180 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{7,15}", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String telefono
) {

    public RegistrarPacienteCommand {
        cedula = cedula == null ? null : cedula.trim().replaceAll("[.\\s-]", "");
        nombreCompleto = nombreCompleto == null ? null : nombreCompleto.trim();
        telefono = telefono == null ? null : telefono.replaceAll("[\\s\\-()]", "");
    }
}
