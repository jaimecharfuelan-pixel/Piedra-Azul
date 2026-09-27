package com.piedraazul.identidad.infraestructura.dto;

import com.piedraazul.nucleo.dominio.RolUsuario;

public record UsuarioResponseDTO(
        Long id,
        String username,
        RolUsuario rol,
        Long personaId,
        boolean activo
) {
}
