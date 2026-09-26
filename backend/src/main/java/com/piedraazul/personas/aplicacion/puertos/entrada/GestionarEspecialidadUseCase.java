package com.piedraazul.personas.aplicacion.puertos.entrada;

import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;

import java.util.List;

public interface GestionarEspecialidadUseCase {
    EspecialidadResponseDTO crear(CrearEspecialidadCommand comando);

    EspecialidadResponseDTO cambiarEstado(Long id, boolean activa);

    List<EspecialidadResponseDTO> listarActivas();
}
