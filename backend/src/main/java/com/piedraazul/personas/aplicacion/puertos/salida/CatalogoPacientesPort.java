package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;

import java.util.Optional;

/**
 * Puerto público consumido por Citas: valida que el paciente exista al agendar
 * (RF2) y resuelve su nombre para el listado del agendador (RF1).
 */
public interface CatalogoPacientesPort {

    boolean existePaciente(Long pacienteId);

    PacienteResumenDTO obtenerResumenPaciente(Long pacienteId);

    /**
     * Variante tolerante para listados: si el paciente ya no existe no rompe la consulta.
     */
    Optional<PacienteResumenDTO> buscarResumenPaciente(Long pacienteId);
}
