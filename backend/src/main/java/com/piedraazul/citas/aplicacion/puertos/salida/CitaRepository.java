package com.piedraazul.citas.aplicacion.puertos.salida;

import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida del módulo Citas hacia la persistencia.
 */
public interface CitaRepository {

    Cita guardar(Cita cita);

    Optional<Cita> buscarPorId(Long id);

    /** RF1 con el orden por defecto (hora ascendente). */
    List<Cita> buscarPorMedicoYFecha(Long medicoId, LocalDate fecha);

    /** RF1 permitiendo cambiar el criterio de ordenamiento. */
    List<Cita> buscarPorMedicoYFecha(Long medicoId, LocalDate fecha, OrdenCitas orden);

    /** Sólo las citas que ocupan agenda (PROGRAMADA), para el cálculo de franjas libres. */
    List<Cita> buscarProgramadasPorMedicoYFecha(Long medicoId, LocalDate fecha);

    /** Alimenta la vista semanal/mensual del calendario. */
    List<Cita> buscarPorMedicoEntreFechas(Long medicoId, LocalDate desde, LocalDate hasta);

    /** "Mis citas" del paciente, de la más reciente a la más antigua. */
    List<Cita> buscarPorPaciente(Long pacienteId);

    boolean existeSolapamiento(Long medicoId, LocalDate fecha, TimeRange rango);

    /**
     * Igual que {@link #existeSolapamiento(Long, LocalDate, TimeRange)} pero ignorando
     * una cita concreta, para que al reagendar no choque consigo misma.
     */
    boolean existeSolapamiento(Long medicoId, LocalDate fecha, TimeRange rango, Long citaIdExcluida);
}
