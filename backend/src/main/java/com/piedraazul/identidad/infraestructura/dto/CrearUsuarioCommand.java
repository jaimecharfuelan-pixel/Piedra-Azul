package com.piedraazul.identidad.infraestructura.dto;

import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioCommand(

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 80, message = "El usuario debe tener entre 3 y 80 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        RolUsuario rol,

        @Valid
        DatosPersonaDTO datosPersona
) {
}
