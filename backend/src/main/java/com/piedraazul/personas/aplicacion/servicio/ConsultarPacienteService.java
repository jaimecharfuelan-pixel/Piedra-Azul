package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.entrada.ConsultarPacienteUseCase;
import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Paciente;
import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ConsultarPacienteService implements ConsultarPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public ConsultarPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public PacienteResponseDTO buscarPorId(Long id) {
        Paciente paciente = pacienteRepository.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "PACIENTE_NO_ENCONTRADO",
                        "No existe paciente con id " + id
                ));
        return toDto(paciente);
    }

    @Override
    public List<PacienteResponseDTO> listarTodos() {
        return pacienteRepository.listarTodos().stream().map(this::toDto).toList();
    }

    private PacienteResponseDTO toDto(Paciente p) {
        return new PacienteResponseDTO(p.getId(), p.getNombreCompleto(), p.getTelefono());
    }
}
