package com.piedraazul.citas.aplicacion.puertos.entrada;

import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;

import java.util.List;

/**
 * Historial de consultas: lo que queda después de atender una cita.
 */
public interface ConsultarHistorialUseCase {

    List<ConsultaResponseDTO> porPaciente(Long pacienteId);

    List<ConsultaResponseDTO> porMedico(Long medicoId);
}
