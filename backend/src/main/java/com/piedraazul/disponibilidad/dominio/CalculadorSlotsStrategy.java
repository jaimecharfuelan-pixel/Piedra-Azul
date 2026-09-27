package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;
import java.util.List;

/**
 * Strategy: único punto de extensión del cálculo de disponibilidad.
 * Si mañana cambia la regla (ej. sobrecupos o slots de duración variable) se
 * agrega otra implementación sin tocar el caso de uso.
 */
public interface CalculadorSlotsStrategy {

    /**
     * @param periodo         horario vigente del médico para esa fecha
     * @param fecha           día que se está consultando
     * @param rangosOcupados  horas que ya tienen cita PROGRAMADA ese día
     * @return franjas libres, ordenadas por hora de inicio
     */
    List<SlotDisponible> calcular(PeriodoDisponibilidad periodo, LocalDate fecha, List<TimeRange> rangosOcupados);
}
