package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.personas.dominio.Especialidad;

import java.util.List;
import java.util.Optional;

public interface EspecialidadRepository {
    Especialidad guardar(Especialidad especialidad);

    Optional<Especialidad> buscarPorId(Long id);

    List<Especialidad> listarActivas();
}
