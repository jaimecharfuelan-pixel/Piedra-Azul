package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.personas.aplicacion.puertos.entrada.RegistrarPacienteUseCase;
import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Paciente;
import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import com.piedraazul.personas.infraestructura.dto.RegistrarPacienteCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Crea el registro de paciente que exige el RF2 para agendar por la web.
 *
 * <p>Hasta que Identidad exponga usuarios reales, se asigna un {@code usuarioId}
 * único de respaldo. Cuando exista el login, este caso de uso delegará en
 * {@code RegistrarPersonaPort} con el id del usuario autenticado.</p>
 */
@Service
@Transactional
public class RegistrarPacienteService implements RegistrarPacienteUseCase {

    private final PacienteRepository pacienteRepository;

    public RegistrarPacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    public PacienteResponseDTO ejecutar(RegistrarPacienteCommand comando) {
        Paciente paciente = pacienteRepository.guardar(
                new Paciente(nuevoUsuarioPlaceholder(), comando.nombreCompleto(), comando.telefono())
        );
        return new PacienteResponseDTO(paciente.getId(), paciente.getNombreCompleto(), paciente.getTelefono());
    }

    private long nuevoUsuarioPlaceholder() {
        long candidato;
        do {
            candidato = Math.abs(UUID.randomUUID().getMostSignificantBits());
            if (candidato == 0L) {
                candidato = 1L;
            }
        } while (pacienteRepository.buscarPorUsuarioId(candidato).isPresent());
        return candidato;
    }
}
