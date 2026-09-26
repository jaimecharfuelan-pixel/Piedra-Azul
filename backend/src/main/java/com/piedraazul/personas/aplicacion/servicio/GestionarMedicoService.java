package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarMedicoUseCase;
import com.piedraazul.personas.aplicacion.puertos.salida.EspecialidadRepository;
import com.piedraazul.personas.aplicacion.puertos.salida.MedicoRepository;
import com.piedraazul.personas.dominio.Especialidad;
import com.piedraazul.personas.dominio.Medico;
import com.piedraazul.personas.infraestructura.dto.ActualizarMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.CrearMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
import com.piedraazul.personas.infraestructura.dto.MedicoResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GestionarMedicoService implements GestionarMedicoUseCase {

    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;

    public GestionarMedicoService(
            MedicoRepository medicoRepository,
            EspecialidadRepository especialidadRepository
    ) {
        this.medicoRepository = medicoRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public MedicoResponseDTO crear(CrearMedicoCommand comando) {
        Especialidad especialidad = requerirEspecialidadActiva(comando.especialidadId());
        Medico guardado = medicoRepository.guardar(new Medico(comando.nombreCompleto(), especialidad.getId()));
        return toDto(guardado, especialidad);
    }

    @Override
    public MedicoResponseDTO actualizar(Long id, ActualizarMedicoCommand comando) {
        Medico medico = requerirMedico(id);
        Especialidad especialidad = requerirEspecialidadActiva(comando.especialidadId());
        medico.actualizar(comando.nombreCompleto(), especialidad.getId());
        return toDto(medicoRepository.guardar(medico), especialidad);
    }

    @Override
    public MedicoResponseDTO cambiarEstado(Long id, boolean activo) {
        Medico medico = requerirMedico(id);
        if (activo) {
            medico.activar();
        } else {
            medico.desactivar();
        }
        Especialidad especialidad = especialidadRepository.buscarPorId(medico.getEspecialidadId())
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "ESPECIALIDAD_NO_ENCONTRADA",
                        "No existe especialidad con id " + medico.getEspecialidadId()
                ));
        return toDto(medicoRepository.guardar(medico), especialidad);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponseDTO buscarPorId(Long id) {
        Medico medico = requerirMedico(id);
        Especialidad especialidad = especialidadRepository.buscarPorId(medico.getEspecialidadId())
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "ESPECIALIDAD_NO_ENCONTRADA",
                        "No existe especialidad con id " + medico.getEspecialidadId()
                ));
        return toDto(medico, especialidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponseDTO> listarActivos() {
        return medicoRepository.listarActivos().stream()
                .map(medico -> {
                    Especialidad especialidad = especialidadRepository.buscarPorId(medico.getEspecialidadId())
                            .orElse(null);
                    return toDto(medico, especialidad);
                })
                .toList();
    }

    private Medico requerirMedico(Long id) {
        return medicoRepository.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "MEDICO_NO_ENCONTRADO",
                        "No existe médico con id " + id
                ));
    }

    private Especialidad requerirEspecialidadActiva(Long especialidadId) {
        Especialidad especialidad = especialidadRepository.buscarPorId(especialidadId)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "ESPECIALIDAD_NO_ENCONTRADA",
                        "No existe especialidad con id " + especialidadId
                ));
        if (!especialidad.isActiva()) {
            throw ReglaDeNegocioException.de(
                    "ESPECIALIDAD_INACTIVA",
                    "No se puede asignar una especialidad inactiva"
            );
        }
        return especialidad;
    }

    private MedicoResponseDTO toDto(Medico medico, Especialidad especialidad) {
        EspecialidadResponseDTO espDto = especialidad == null
                ? null
                : new EspecialidadResponseDTO(especialidad.getId(), especialidad.getNombre(), especialidad.isActiva());
        return new MedicoResponseDTO(medico.getId(), medico.getNombreCompleto(), espDto, medico.estaActivo());
    }
}
