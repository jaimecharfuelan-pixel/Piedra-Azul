package com.piedraazul.personas.aplicacion.puertos.entrada;

import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import com.piedraazul.personas.infraestructura.dto.RegistrarPacienteCommand;

/**
 * RF2: el paciente se registra en la web antes de poder agendar.
 */
public interface RegistrarPacienteUseCase {

    PacienteResponseDTO ejecutar(RegistrarPacienteCommand comando);
}
