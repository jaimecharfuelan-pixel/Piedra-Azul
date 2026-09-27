package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.aplicacion.puertos.salida.ConsultarCitasPort;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.nucleo.dominio.TimeRange;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Fachada pública del módulo Citas hacia Disponibilidad.
 */
@Service
@Transactional(readOnly = true)
public class CitasModuleFacade implements ConsultarCitasPort {

    private final CitaRepository citaRepository;

    public CitasModuleFacade(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public List<TimeRange> obtenerRangosOcupados(Long medicoId, LocalDate fecha) {
        return citaRepository.buscarProgramadasPorMedicoYFecha(medicoId, fecha).stream()
                .map(Cita::getRango)
                .toList();
    }
}
