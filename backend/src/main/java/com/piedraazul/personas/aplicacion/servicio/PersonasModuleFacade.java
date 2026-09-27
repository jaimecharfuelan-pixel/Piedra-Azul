package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.aplicacion.puertos.salida.MedicoRepository;
import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.aplicacion.puertos.salida.RegistrarPersonaPort;
import com.piedraazul.personas.dominio.Medico;
import com.piedraazul.personas.dominio.Paciente;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Fachada pública del módulo Personas hacia Identidad, Disponibilidad y Citas.
 */
@Service
@Transactional
public class PersonasModuleFacade implements CatalogoMedicosPort, CatalogoPacientesPort, RegistrarPersonaPort {

    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;

    public PersonasModuleFacade(MedicoRepository medicoRepository, PacienteRepository pacienteRepository) {
        this.medicoRepository = medicoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeMedicoActivo(Long medicoId) {
        return medicoRepository.buscarPorId(medicoId).map(Medico::estaActivo).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResumenDTO obtenerResumen(Long medicoId) {
        Medico medico = medicoRepository.buscarPorId(medicoId)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "MEDICO_NO_ENCONTRADO",
                        "No existe médico con id " + medicoId
                ));
        return new MedicoResumenDTO(medico.getId(), medico.getNombreCompleto());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePaciente(Long pacienteId) {
        return pacienteId != null && pacienteRepository.buscarPorId(pacienteId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public PacienteResumenDTO obtenerResumenPaciente(Long pacienteId) {
        return buscarResumenPaciente(pacienteId)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "PACIENTE_NO_ENCONTRADO",
                        "No existe paciente con id " + pacienteId
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PacienteResumenDTO> buscarResumenPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return Optional.empty();
        }
        return pacienteRepository.buscarPorId(pacienteId)
                .map(paciente -> new PacienteResumenDTO(
                        paciente.getId(),
                        paciente.getNombreCompleto(),
                        paciente.getTelefono()
                ));
    }

    @Override
    public Long crearPersonaParaUsuario(RolUsuario rol, DatosPersonaDTO datos) {
        if (rol == RolUsuario.MEDICO) {
            Medico medico = medicoRepository.guardar(
                    new Medico(datos.nombreCompleto(), datos.especialidadId())
            );
            return medico.getId();
        }
        if (rol == RolUsuario.PACIENTE) {
            Paciente paciente = pacienteRepository.guardar(
                    new Paciente(datos.usuarioId(), datos.nombreCompleto(), datos.telefono())
            );
            return paciente.getId();
        }
        // ADMINISTRADOR y AGENDADOR no tienen ficha en Personas (diagrama Identidad).
        return null;
    }
}
