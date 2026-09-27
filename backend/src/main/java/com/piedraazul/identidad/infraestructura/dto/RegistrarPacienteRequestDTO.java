package com.piedraazul.identidad.infraestructura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registro público del paciente (RF2): crea Usuario PACIENTE + ficha Paciente.
 */
public record RegistrarPacienteRequestDTO(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 80, message = "El usuario debe tener entre 3 y 80 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 180, message = "El nombre debe tener entre 3 y 180 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "\\d{7,15}", message = "El teléfono debe tener entre 7 y 15 dígitos")
        String telefono
) {

    public RegistrarPacienteRequestDTO {
        username = username == null ? null : username.trim();
        nombreCompleto = nombreCompleto == null ? null : nombreCompleto.trim();
        telefono = telefono == null ? null : telefono.replaceAll("[\\s\\-()]", "");
    }
}
