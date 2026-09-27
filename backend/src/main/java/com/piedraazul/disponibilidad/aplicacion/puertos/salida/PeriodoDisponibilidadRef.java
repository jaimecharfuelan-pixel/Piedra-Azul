package com.piedraazul.disponibilidad.aplicacion.puertos.salida;

import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;

/**
 * Vista de sólo lectura de un periodo de disponibilidad, publicada hacia otros
 * módulos (la implementa la entidad de dominio {@code PeriodoDisponibilidad}).
 *
 * <p>Existe para que Citas pueda preguntar "¿esta hora cabe en el horario del
 * médico?" sin importar la entidad ni el repositorio del módulo Disponibilidad.</p>
 */
public interface PeriodoDisponibilidadRef {

    Long getMedicoId();

    LocalDate getFechaInicio();

    LocalDate getFechaFin();

    /** El periodo está vigente en esa fecha del calendario. */
    boolean incluyeFecha(LocalDate fecha);

    /** El médico atiende ese día de la semana. */
    boolean atiendeEnDia(DiaSemana dia);

    TimeRange getFranjaHoraria();

    int getDuracionCitaMinutos();

    /** Minutos de descanso que el médico deja entre una cita y la siguiente (RF3). */
    int getDescansoEntreCitasMinutos();

    /** Paso de la rejilla de slots: duración de la cita más el descanso. */
    int getPasoMinutos();

    /** El rango solicitado cae completo dentro de la franja horaria de atención. */
    boolean cubreRango(TimeRange rango);

    /**
     * El rango solicitado coincide con un slot exacto de la rejilla: misma duración
     * que la cita configurada y empezando en un múltiplo del paso.
     */
    boolean coincideConRejilla(TimeRange rango);
}
