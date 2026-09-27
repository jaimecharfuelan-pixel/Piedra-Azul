package com.piedraazul.citas.aplicacion.puertos.entrada;

import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ReagendarCitaCommand;

import java.util.List;

/**
 * RF2 y ciclo de vida completo de la cita.
 *
 * <p>Los roles se validan en el Controller/JWT, el caso de uso no sabe de roles:
 * agendar lo hace el Paciente para sí mismo o el Agendador para cualquiera;
 * cancelar y reagendar el dueño de la cita, el Agendador o el Admin;
 * marcarAtendida sólo el Médico.</p>
 */
public interface GestionarCitaUseCase {

    CitaResponseDTO agendar(AgendarCitaCommand comando);

    CitaResponseDTO cancelar(Long citaId);

    CitaResponseDTO reagendar(Long citaId, ReagendarCitaCommand comando);

    ConsultaResponseDTO marcarAtendida(Long citaId, String observaciones);

    CitaResponseDTO buscarPorId(Long citaId);

    /** "Mis citas": lo que el paciente ve para poder cancelar o reagendar. */
    List<CitaResponseDTO> listarPorPaciente(Long pacienteId);
}
