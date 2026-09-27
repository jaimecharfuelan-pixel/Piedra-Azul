package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CitaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 2, 10);
    private static final TimeRange OCHO_A_OCHO_Y_MEDIA =
            new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30));

    private static Cita citaNueva() {
        return Cita.crear(5L, 9L, FECHA, OCHO_A_OCHO_Y_MEDIA);
    }

    @Test
    @DisplayName("una cita nueva nace PROGRAMADA")
    void naceProgramada() {
        Cita cita = citaNueva();

        assertEquals(EstadoCita.PROGRAMADA, cita.getEstado());
        assertTrue(cita.estaProgramada());
        assertEquals(5L, cita.getPacienteId());
        assertEquals(9L, cita.getMedicoId());
    }

    @Test
    @DisplayName("cancelar pasa la cita a CANCELADA")
    void cancelar() {
        Cita cita = citaNueva();

        cita.cancelar();

        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        assertFalse(cita.estaProgramada());
    }

    @Test
    @DisplayName("reagendar cambia fecha y franja manteniendo el estado PROGRAMADA")
    void reagendar() {
        Cita cita = citaNueva();
        TimeRange nuevoRango = new TimeRange(LocalTime.of(10, 0), LocalTime.of(10, 30));

        cita.reagendar(FECHA.plusDays(2), nuevoRango);

        assertEquals(FECHA.plusDays(2), cita.getFecha());
        assertEquals(nuevoRango, cita.getRango());
        assertTrue(cita.estaProgramada());
    }

    @Test
    @DisplayName("marcarComoAtendida pasa la cita a ATENDIDA")
    void marcarComoAtendida() {
        Cita cita = citaNueva();

        cita.marcarComoAtendida();

        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    @DisplayName("una cita cancelada ya no se puede cancelar, reagendar ni atender")
    void canceladaEsInmutable() {
        Cita cita = citaNueva();
        cita.cancelar();

        assertEquals("CITA_NO_MODIFICABLE", assertThrows(ReglaDeNegocioException.class, cita::cancelar).getCodigo());
        assertEquals("CITA_NO_MODIFICABLE",
                assertThrows(ReglaDeNegocioException.class, cita::marcarComoAtendida).getCodigo());
        assertEquals("CITA_NO_MODIFICABLE", assertThrows(
                ReglaDeNegocioException.class,
                () -> cita.reagendar(FECHA.plusDays(1), OCHO_A_OCHO_Y_MEDIA)
        ).getCodigo());
    }

    @Test
    @DisplayName("una cita atendida ya no se puede modificar")
    void atendidaEsInmutable() {
        Cita cita = citaNueva();
        cita.marcarComoAtendida();

        assertEquals("CITA_NO_MODIFICABLE", assertThrows(ReglaDeNegocioException.class, cita::cancelar).getCodigo());
    }

    @Test
    @DisplayName("una cita reconstituida conserva su estado final")
    void reconstituirConservaEstado() {
        Cita cita = Cita.reconstituir(3L, 5L, 9L, FECHA, OCHO_A_OCHO_Y_MEDIA, EstadoCita.ATENDIDA);

        assertEquals(3L, cita.getId());
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
        assertFalse(cita.estaProgramada());
    }
}
