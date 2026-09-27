package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.ConsultarConfiguracionPort;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.PeriodoDisponibilidadRef;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Fachada pública del módulo Disponibilidad hacia Citas.
 */
@Service
@Transactional(readOnly = true)
public class DisponibilidadModuleFacade implements ConsultarConfiguracionPort {

    private final DisponibilidadRepository disponibilidadRepository;

    public DisponibilidadModuleFacade(DisponibilidadRepository disponibilidadRepository) {
        this.disponibilidadRepository = disponibilidadRepository;
    }

    @Override
    public int obtenerVentanaSemanas() {
        return disponibilidadRepository.obtenerConfiguracionSistema().getVentanaSemanas();
    }

    @Override
    public PeriodoDisponibilidadRef obtenerPeriodoVigente(Long medicoId, LocalDate fecha) {
        return disponibilidadRepository.buscarPeriodoVigente(medicoId, fecha)
                .orElseThrow(() -> ReglaDeNegocioException.de(
                        "MEDICO_NO_DISPONIBLE",
                        "El médico no tiene horario de atención configurado para el " + fecha
                ));
    }
}
