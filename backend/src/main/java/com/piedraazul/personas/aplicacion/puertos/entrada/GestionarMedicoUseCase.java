package com.piedraazul.personas.aplicacion.puertos.entrada;

import com.piedraazul.personas.infraestructura.dto.ActualizarMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.CrearMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.MedicoResponseDTO;

import java.util.List;

public interface GestionarMedicoUseCase {
    MedicoResponseDTO crear(CrearMedicoCommand comando);

    MedicoResponseDTO actualizar(Long id, ActualizarMedicoCommand comando);

    MedicoResponseDTO cambiarEstado(Long id, boolean activo);

    MedicoResponseDTO buscarPorId(Long id);

    List<MedicoResponseDTO> listarActivos();
}
