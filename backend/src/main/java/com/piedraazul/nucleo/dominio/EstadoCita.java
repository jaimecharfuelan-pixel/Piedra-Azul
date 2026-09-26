package com.piedraazul.nucleo.dominio;

/**
 * Ciclo de vida de una cita.
 * {@code ATENDIDA} indica que la cita se cumplió y puede disparar la creación de una Consulta.
 */
public enum EstadoCita {
    PROGRAMADA,
    CANCELADA,
    ATENDIDA
}
