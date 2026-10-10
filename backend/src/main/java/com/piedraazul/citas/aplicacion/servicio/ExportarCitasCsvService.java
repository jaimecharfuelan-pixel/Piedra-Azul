package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.entrada.ExportarCitasCsvUseCase;
import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Genera un CSV de la agenda del día para médico/terapeuta/agendador.
 */
@Service
@Transactional(readOnly = true)
public class ExportarCitasCsvService implements ExportarCitasCsvUseCase {

    private static final String HEADER =
            "horaInicio,horaFin,pacienteNombre,pacienteTelefono,pacienteCedula,estado";

    private final CitaRepository citaRepository;
    private final CatalogoPacientesPort catalogoPacientesPort;

    public ExportarCitasCsvService(CitaRepository citaRepository, CatalogoPacientesPort catalogoPacientesPort) {
        this.citaRepository = citaRepository;
        this.catalogoPacientesPort = catalogoPacientesPort;
    }

    @Override
    public String ejecutar(Long medicoId, LocalDate fecha) {
        List<Cita> citas = citaRepository.buscarPorMedicoYFecha(medicoId, fecha);
        StringBuilder csv = new StringBuilder(HEADER).append('\n');
        for (Cita cita : citas) {
            PacienteResumenDTO paciente = catalogoPacientesPort.buscarResumenPaciente(cita.getPacienteId())
                    .orElseGet(() -> new PacienteResumenDTO(
                            cita.getPacienteId(),
                            "",
                            "Paciente #" + cita.getPacienteId(),
                            ""
                    ));
            csv.append(formatearFila(cita, paciente)).append('\n');
        }
        return csv.toString();
    }

    private String formatearFila(Cita cita, PacienteResumenDTO paciente) {
        return String.join(
                ",",
                escapar(cita.getRango().getHoraInicio().toString()),
                escapar(cita.getRango().getHoraFin().toString()),
                escapar(paciente.nombreCompleto()),
                escapar(paciente.telefono()),
                escapar(paciente.cedula() == null ? "" : paciente.cedula()),
                escapar(cita.getEstado().name())
        );
    }

    private static String escapar(String valor) {
        if (valor == null) {
            return "\"\"";
        }
        String limpio = valor.replace("\"", "\"\"");
        return "\"" + limpio + "\"";
    }
}
