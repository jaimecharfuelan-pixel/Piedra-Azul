package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlotDisponibleTest {

    @Test
    void conservaFechaYRango() {
        LocalDate fecha = LocalDate.of(2026, 9, 28);
        TimeRange rango = new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30));

        SlotDisponible slot = new SlotDisponible(fecha, rango);

        assertEquals(fecha, slot.fecha());
        assertEquals(rango, slot.rango());
    }
}
