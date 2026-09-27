package com.piedraazul.identidad.infraestructura.dto;

import com.piedraazul.nucleo.dominio.RolUsuario;

public record TokenResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn,
        RolUsuario rol,
        Long personaId,
        Long usuarioId
) {
}
