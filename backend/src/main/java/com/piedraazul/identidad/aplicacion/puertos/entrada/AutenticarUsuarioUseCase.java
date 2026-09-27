package com.piedraazul.identidad.aplicacion.puertos.entrada;

import com.piedraazul.identidad.infraestructura.dto.TokenResponseDTO;

public interface AutenticarUsuarioUseCase {

    TokenResponseDTO ejecutar(String username, String passwordPlano);
}
