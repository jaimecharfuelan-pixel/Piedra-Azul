package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.Consulta;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Arma los DTO de salida resolviendo los nombres de médico y paciente a través
 * de los puertos públicos del módulo Personas.
 *
 * <p>Al construir un listado se cachean los nombres dentro de la llamada para no
 * repetir una consulta por fila.</p>
 */
@Component
public class CitaDtoAssembler {

    private final CatalogoMedicosPort catalogoMedicosPort;
    private final CatalogoPacientesPort catalogoPacientesPort;

    public CitaDtoAssembler(CatalogoMedicosPort catalogoMedicosPort, CatalogoPacientesPort catalogoPacientesPort) {
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.catalogoPacientesPort = catalogoPacientesPort;
    }

    public CitaResponseDTO toDto(Cita cita) {
        return toDto(cita, new Cache());
    }

    public List<CitaResponseDTO> toDtos(List<Cita> citas) {
        Cache cache = new Cache();
        return citas.stream().map(cita -> toDto(cita, cache)).toList();
    }

    public ConsultaResponseDTO toDto(Consulta consulta) {
        Cache cache = new Cache();
        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getCitaId(),
                consulta.getMedicoId(),
                cache.medico(consulta.getMedicoId()),
                consulta.getPacienteId(),
                cache.paciente(consulta.getPacienteId()).nombreCompleto(),
                consulta.getFecha(),
                consulta.getObservaciones(),
                consulta.getFechaRegistro()
        );
    }

    public List<ConsultaResponseDTO> toDtosConsulta(List<Consulta> consultas) {
        Cache cache = new Cache();
        return consultas.stream()
                .map(consulta -> new ConsultaResponseDTO(
                        consulta.getId(),
                        consulta.getCitaId(),
                        consulta.getMedicoId(),
                        cache.medico(consulta.getMedicoId()),
                        consulta.getPacienteId(),
                        cache.paciente(consulta.getPacienteId()).nombreCompleto(),
                        consulta.getFecha(),
                        consulta.getObservaciones(),
                        consulta.getFechaRegistro()
                ))
                .toList();
    }

    private CitaResponseDTO toDto(Cita cita, Cache cache) {
        PacienteResumenDTO paciente = cache.paciente(cita.getPacienteId());
        return new CitaResponseDTO(
                cita.getId(),
                cita.getMedicoId(),
                cache.medico(cita.getMedicoId()),
                cita.getPacienteId(),
                paciente.nombreCompleto(),
                paciente.telefono(),
                cita.getFecha(),
                cita.getRango().getHoraInicio(),
                cita.getRango().getHoraFin(),
                cita.getRango().duracionMinutos(),
                cita.getEstado()
        );
    }

    /** Memoria de nombres válida durante una sola construcción de respuesta. */
    private final class Cache {

        private final Map<Long, String> medicos = new HashMap<>();
        private final Map<Long, PacienteResumenDTO> pacientes = new HashMap<>();

        String medico(Long medicoId) {
            return medicos.computeIfAbsent(
                    medicoId,
                    id -> catalogoMedicosPort.obtenerResumen(id).nombreCompleto()
            );
        }

        PacienteResumenDTO paciente(Long pacienteId) {
            return pacientes.computeIfAbsent(
                    pacienteId,
                    id -> catalogoPacientesPort.buscarResumenPaciente(id)
                            .orElseGet(() -> new PacienteResumenDTO(id, "Paciente #" + id, ""))
            );
        }
    }
}
