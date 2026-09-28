package com.piedraazul.nucleo.dominio;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiaSemanaTest {

    @Test
    void traduceElDiaDeJavaTime() {
        assertEquals(DiaSemana.LUNES, DiaSemana.desde(DayOfWeek.MONDAY));
        assertEquals(DiaSemana.DOMINGO, DiaSemana.desde(DayOfWeek.SUNDAY));
    }

    @Test
    void traduceUnaFecha() {
        assertEquals(DiaSemana.LUNES, DiaSemana.desde(LocalDate.of(2026, 9, 28)));
    }

    @Test
    void rechazaNulos() {
        assertThrows(NullPointerException.class, () -> DiaSemana.desde((DayOfWeek) null));
        assertThrows(NullPointerException.class, () -> DiaSemana.desde((LocalDate) null));
    }

    @Test
    void etiquetaEsLegible() {
        assertEquals("Miércoles", DiaSemana.MIERCOLES.etiqueta());
        assertEquals("Sábado", DiaSemana.SABADO.etiqueta());
    }
}
