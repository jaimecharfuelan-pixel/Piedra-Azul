package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.personas.dominio.Medico;

import java.util.List;
import java.util.Optional;

public interface MedicoRepository {
    Medico guardar(Medico medico);

    Optional<Medico> buscarPorId(Long id);

    List<Medico> listarActivos();
}
