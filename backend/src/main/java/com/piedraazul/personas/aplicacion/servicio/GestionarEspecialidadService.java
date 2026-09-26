package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarEspecialidadUseCase;
import com.piedraazul.personas.aplicacion.puertos.salida.EspecialidadRepository;
import com.piedraazul.personas.dominio.Especialidad;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class GestionarEspecialidadService implements GestionarEspecialidadUseCase {

    private final EspecialidadRepository especialidadRepository;

    public GestionarEspecialidadService(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public EspecialidadResponseDTO crear(CrearEspecialidadCommand comando) {
        Especialidad guardada = especialidadRepository.guardar(new Especialidad(comando.nombre()));
        return toDto(guardada);
    }

    @Override
    public EspecialidadResponseDTO cambiarEstado(Long id, boolean activa) {
        Especialidad especialidad = especialidadRepository.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "ESPECIALIDAD_NO_ENCONTRADA",
                        "No existe especialidad con id " + id
                ));
        if (activa) {
            especialidad.activar();
        } else {
            especialidad.desactivar();
        }
        return toDto(especialidadRepository.guardar(especialidad));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponseDTO> listarActivas() {
        return especialidadRepository.listarActivas().stream().map(this::toDto).toList();
    }

    private EspecialidadResponseDTO toDto(Especialidad e) {
        return new EspecialidadResponseDTO(e.getId(), e.getNombre(), e.isActiva());
    }
}
