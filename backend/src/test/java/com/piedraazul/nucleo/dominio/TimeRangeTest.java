package com.piedraazul.nucleo.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeTest {

    @Test
    void creaRangoValidoDeAlMenos30Minutos() {
        TimeRange rango = new TimeRange(LocalTime.of(9, 0), LocalTime.of(9, 30));
        assertEquals(30, rango.duracionMinutos());
    }

    @Test
    void rechazaDuracionMenorA30Minutos() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new TimeRange(LocalTime.of(9, 0), LocalTime.of(9, 15))
        );
        assertEquals("DURACION_INVALIDA", ex.getCodigo());
    }

    @Test
    void detectaSolapamiento() {
        TimeRange a = new TimeRange(LocalTime.of(9, 0), LocalTime.of(10, 0));
        TimeRange b = new TimeRange(LocalTime.of(9, 30), LocalTime.of(10, 30));
        TimeRange c = new TimeRange(LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertTrue(a.solapaCon(b));
        assertFalse(a.solapaCon(c));
    }
}
