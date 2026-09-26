package com.piedraazul.personas.aplicacion.puertos.entrada;

import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;

import java.util.List;

public interface ConsultarPacienteUseCase {
    PacienteResponseDTO buscarPorId(Long id);

    List<PacienteResponseDTO> listarTodos();
}
