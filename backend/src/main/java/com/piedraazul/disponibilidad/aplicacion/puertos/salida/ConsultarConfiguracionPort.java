package com.piedraazul.disponibilidad.aplicacion.puertos.salida;

import java.time.LocalDate;

/**
 * Puerto público del módulo Disponibilidad.
 *
 * <p>Consumido por Citas para validar que la hora solicitada caiga dentro del
 * horario vigente del médico y dentro de la ventana de agendamiento (RF3).</p>
 */
public interface ConsultarConfiguracionPort {

    int obtenerVentanaSemanas();

    /**
     * @throws com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException
     *         con código {@code MEDICO_NO_DISPONIBLE} si el médico no tiene horario vigente esa fecha
     */
    PeriodoDisponibilidadRef obtenerPeriodoVigente(Long medicoId, LocalDate fecha);
}
