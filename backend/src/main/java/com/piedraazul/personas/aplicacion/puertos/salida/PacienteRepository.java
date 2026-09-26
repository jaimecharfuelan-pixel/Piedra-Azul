package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.personas.dominio.Paciente;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository {
    Paciente guardar(Paciente paciente);

    Optional<Paciente> buscarPorId(Long id);

    Optional<Paciente> buscarPorUsuarioId(Long usuarioId);

    List<Paciente> listarTodos();
}
