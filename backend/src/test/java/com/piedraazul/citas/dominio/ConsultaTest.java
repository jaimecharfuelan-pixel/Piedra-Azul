package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConsultaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 2, 10);
    private static final TimeRange RANGO = new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30));

    @Test
    @DisplayName("se crea desde una cita atendida copiando médico, paciente y fecha")
    void desdeCitaAtendida() {
        Cita cita = Cita.reconstituir(4L, 5L, 9L, FECHA, RANGO, EstadoCita.ATENDIDA);

        Consulta consulta = Consulta.desdeCita(cita, "  Control sin novedades  ");

        assertEquals(4L, consulta.getCitaId());
        assertEquals(9L, consulta.getMedicoId());
        assertEquals(5L, consulta.getPacienteId());
        assertEquals(FECHA, consulta.getFecha());
        assertEquals("Control sin novedades", consulta.getObservaciones());
        assertNotNull(consulta.getFechaRegistro());
    }

    @Test
    @DisplayName("no se crea consulta de una cita que no está atendida")
    void rechazaCitaNoAtendida() {
        Cita programada = Cita.reconstituir(4L, 5L, 9L, FECHA, RANGO, EstadoCita.PROGRAMADA);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> Consulta.desdeCita(programada, "algo")
        );
        assertEquals("CONSULTA_SIN_CITA_ATENDIDA", ex.getCodigo());
    }

    @Test
    @DisplayName("observaciones nulas quedan como cadena vacía")
    void observacionesNulas() {
        Cita cita = Cita.reconstituir(4L, 5L, 9L, FECHA, RANGO, EstadoCita.ATENDIDA);

        assertEquals("", Consulta.desdeCita(cita, null).getObservaciones());
    }

    @Test
    @DisplayName("rechaza observaciones más largas que el límite")
    void rechazaObservacionesLargas() {
        Cita cita = Cita.reconstituir(4L, 5L, 9L, FECHA, RANGO, EstadoCita.ATENDIDA);
        String demasiado = "x".repeat(Consulta.MAXIMO_OBSERVACIONES + 1);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> Consulta.desdeCita(cita, demasiado)
        );
        assertEquals("OBSERVACIONES_DEMASIADO_LARGAS", ex.getCodigo());
    }
}
