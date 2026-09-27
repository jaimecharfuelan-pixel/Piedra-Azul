package com.piedraazul.personas.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registro web del paciente (RF2). El módulo Identidad aún no expone login,
 * así que aquí se crea el Paciente con un {@code usuarioId} temporal.
 */
public record RegistrarPacienteCommand(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 180, message = "El nombre debe tener entre 3 y 180 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{7,15}", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String telefono
) {

    public RegistrarPacienteCommand {
        nombreCompleto = nombreCompleto == null ? null : nombreCompleto.trim();
        telefono = telefono == null ? null : telefono.replaceAll("[\\s\\-()]", "");
    }
}
