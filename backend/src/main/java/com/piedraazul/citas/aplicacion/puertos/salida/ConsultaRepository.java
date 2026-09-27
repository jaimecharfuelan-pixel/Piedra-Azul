package com.piedraazul.citas.aplicacion.puertos.salida;

import com.piedraazul.citas.dominio.Consulta;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para el historial de consultas.
 */
public interface ConsultaRepository {

    Consulta guardar(Consulta consulta);

    List<Consulta> listarPorPaciente(Long pacienteId);

    List<Consulta> listarPorMedico(Long medicoId);

    /** Evita registrar dos consultas para la misma cita. */
    Optional<Consulta> buscarPorCitaId(Long citaId);
}
