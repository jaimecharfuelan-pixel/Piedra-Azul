package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarPeriodosDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * RF3 (lectura): historial de horarios de un médico.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarPeriodosDisponibilidadService implements ConsultarPeriodosDisponibilidadUseCase {

    private final DisponibilidadRepository disponibilidadRepository;
    private final CatalogoMedicosPort catalogoMedicosPort;
    private final Clock clock;

    public ConsultarPeriodosDisponibilidadService(
            DisponibilidadRepository disponibilidadRepository,
            CatalogoMedicosPort catalogoMedicosPort,
            Clock clock
    ) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.clock = clock;
    }

    @Override
    public List<PeriodoDisponibilidadResponseDTO> porMedico(Long medicoId) {
        String nombre = catalogoMedicosPort.obtenerResumen(medicoId).nombreCompleto();
        LocalDate hoy = LocalDate.now(clock);
        return disponibilidadRepository.listarPeriodosPorMedico(medicoId).stream()
                .map(periodo -> DisponibilidadMapper.toDto(periodo, nombre, hoy))
                .toList();
    }
}
