package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.entrada.ConsultarCitasPorCedulaUseCase;
import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.HistorialCitasPorCedulaResponseDTO;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Historial de citas por cédula. Sin JWT en el caso de uso (autorización en borde / Keycloak).
 */
@Service
@Transactional(readOnly = true)
public class ConsultarCitasPorCedulaService implements ConsultarCitasPorCedulaUseCase {

    private final CitaRepository citaRepository;
    private final CatalogoPacientesPort catalogoPacientesPort;
    private final CitaDtoAssembler assembler;

    public ConsultarCitasPorCedulaService(
            CitaRepository citaRepository,
            CatalogoPacientesPort catalogoPacientesPort,
            CitaDtoAssembler assembler
    ) {
        this.citaRepository = citaRepository;
        this.catalogoPacientesPort = catalogoPacientesPort;
        this.assembler = assembler;
    }

    @Override
    public HistorialCitasPorCedulaResponseDTO ejecutar(String cedula) {
        Long pacienteId = catalogoPacientesPort.buscarIdPorCedula(cedula)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "PACIENTE_NO_ENCONTRADO",
                        "No existe paciente con cédula " + cedula
                ));
        PacienteResumenDTO resumen = catalogoPacientesPort.obtenerResumenPaciente(pacienteId);
        List<Cita> citas = citaRepository.buscarPorPaciente(pacienteId);
        List<CitaResponseDTO> dtos = assembler.toDtos(citas);
        return new HistorialCitasPorCedulaResponseDTO(
                resumen.cedula(),
                resumen.id(),
                resumen.nombreCompleto(),
                dtos.size(),
                dtos
        );
    }
}
