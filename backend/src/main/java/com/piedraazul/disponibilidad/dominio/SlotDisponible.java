package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.TimeRange;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Franja concreta y libre que un paciente puede elegir al agendar (RF2).
 * Es un Value Object: no se persiste, se calcula al momento.
 */
public record SlotDisponible(LocalDate fecha, TimeRange rango) {

    public SlotDisponible {
        Objects.requireNonNull(fecha, "fecha es obligatoria");
        Objects.requireNonNull(rango, "rango es obligatorio");
    }
}
