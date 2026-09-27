package com.piedraazul.disponibilidad.aplicacion.puertos.salida;

import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del módulo Disponibilidad hacia la persistencia.
 */
public interface DisponibilidadRepository {

    /**
     * Devuelve la única fila de configuración; si aún no existe, entrega los valores por defecto.
     */
    ConfiguracionSistema obtenerConfiguracionSistema();

    ConfiguracionSistema guardarConfiguracionSistema(ConfiguracionSistema config);

    PeriodoDisponibilidad guardarPeriodo(PeriodoDisponibilidad periodo);

    Optional<PeriodoDisponibilidad> buscarPeriodoVigente(Long medicoId, LocalDate fecha);

    /** Periodo del médico que quedó sin fecha de fin (el que hay que cerrar). */
    Optional<PeriodoDisponibilidad> buscarPeriodoAbiertoAnterior(Long medicoId);

    /** Historial completo de horarios del médico, del más reciente al más antiguo. */
    List<PeriodoDisponibilidad> listarPeriodosPorMedico(Long medicoId);
}
