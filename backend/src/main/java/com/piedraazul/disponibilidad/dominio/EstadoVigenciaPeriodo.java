package com.piedraazul.disponibilidad.dominio;

/**
 * Cómo se muestra un horario respecto a la fecha de hoy de la clínica.
 */
public enum EstadoVigenciaPeriodo {
    /** Hoy cae dentro del rango del periodo. */
    VIGENTE,
    /** La fecha de inicio todavía no llega. */
    FUTURO,
    /** La fecha de fin ya pasó. */
    HISTORICO
}
