package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.entrada.ConsultarHistorialUseCase;
import com.piedraazul.citas.aplicacion.puertos.salida.ConsultaRepository;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Historial de consultas de un paciente o de un médico.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarHistorialService implements ConsultarHistorialUseCase {

    private final ConsultaRepository consultaRepository;
    private final CitaDtoAssembler assembler;

    public ConsultarHistorialService(ConsultaRepository consultaRepository, CitaDtoAssembler assembler) {
        this.consultaRepository = consultaRepository;
        this.assembler = assembler;
    }

    @Override
    public List<ConsultaResponseDTO> porPaciente(Long pacienteId) {
        return assembler.toDtosConsulta(consultaRepository.listarPorPaciente(pacienteId));
    }

    @Override
    public List<ConsultaResponseDTO> porMedico(Long medicoId) {
        return assembler.toDtosConsulta(consultaRepository.listarPorMedico(medicoId));
    }
}
