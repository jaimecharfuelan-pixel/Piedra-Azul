package com.piedraazul.citas.aplicacion.puertos.salida;

import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto público del módulo Citas.
 *
 * <p>Consumido por Disponibilidad para saber qué horas ya están tomadas al
 * calcular las franjas libres. Sólo cuentan las citas PROGRAMADA: al cancelar
 * una cita su franja vuelve a ofrecerse.</p>
 */
public interface ConsultarCitasPort {

    List<TimeRange> obtenerRangosOcupados(Long medicoId, LocalDate fecha);
}
