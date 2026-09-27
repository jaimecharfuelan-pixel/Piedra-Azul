package com.piedraazul.nucleo.dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Días de la semana usados en disponibilidad y franjas horarias (RF3).
 */
public enum DiaSemana {
    LUNES,
    MARTES,
    MIERCOLES,
    JUEVES,
    VIERNES,
    SABADO,
    DOMINGO;

    /**
     * Traduce el {@link DayOfWeek} de java.time al enum del dominio.
     * El orden de las constantes coincide con {@code DayOfWeek.getValue()} (1 = lunes).
     */
    public static DiaSemana desde(DayOfWeek dayOfWeek) {
        Objects.requireNonNull(dayOfWeek, "dayOfWeek es obligatorio");
        return values()[dayOfWeek.getValue() - 1];
    }

    public static DiaSemana desde(LocalDate fecha) {
        Objects.requireNonNull(fecha, "fecha es obligatoria");
        return desde(fecha.getDayOfWeek());
    }

    /**
     * Nombre legible para la interfaz de usuario y los mensajes de error.
     */
    public String etiqueta() {
        return switch (this) {
            case LUNES -> "Lunes";
            case MARTES -> "Martes";
            case MIERCOLES -> "Miércoles";
            case JUEVES -> "Jueves";
            case VIERNES -> "Viernes";
            case SABADO -> "Sábado";
            case DOMINGO -> "Domingo";
        };
    }
}
