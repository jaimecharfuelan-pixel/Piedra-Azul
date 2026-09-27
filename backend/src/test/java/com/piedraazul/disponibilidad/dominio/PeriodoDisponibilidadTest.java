package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeriodoDisponibilidadTest {

    private static final LocalDate LUNES_5_ENERO_2026 = LocalDate.of(2026, 1, 5);
    private static final Set<DiaSemana> LUNES_Y_MIERCOLES = EnumSet.of(DiaSemana.LUNES, DiaSemana.MIERCOLES);

    private static PeriodoDisponibilidad periodoAbierto() {
        return new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                LUNES_Y_MIERCOLES,
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                30,
                0
        );
    }

    @Test
    @DisplayName("rechaza una duración de cita menor a 30 minutos")
    void rechazaDuracionMenorAMedia() {
        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                LUNES_Y_MIERCOLES,
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                20,
                0
        ));
        assertEquals("DURACION_CITA_INVALIDA", ex.getCodigo());
    }

    @Test
    @DisplayName("rechaza una cita que no cabe en la franja horaria")
    void rechazaCitaMasLargaQueLaFranja() {
        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                LUNES_Y_MIERCOLES,
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(9, 0)),
                120,
                0
        ));
        assertEquals("DURACION_CITA_INVALIDA", ex.getCodigo());
    }

    @Test
    @DisplayName("exige al menos un día de atención")
    void exigeDiasDeAtencion() {
        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                EnumSet.noneOf(DiaSemana.class),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                30,
                0
        ));
        assertEquals("PERIODO_SIN_DIAS_ATENCION", ex.getCodigo());
    }

    @Test
    @DisplayName("un periodo abierto incluye cualquier fecha posterior al inicio")
    void periodoAbiertoIncluyeFuturo() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        assertTrue(periodo.estaAbierto());
        assertTrue(periodo.incluyeFecha(LUNES_5_ENERO_2026));
        assertTrue(periodo.incluyeFecha(LUNES_5_ENERO_2026.plusYears(2)));
        assertFalse(periodo.incluyeFecha(LUNES_5_ENERO_2026.minusDays(1)));
    }

    @Test
    @DisplayName("cerrarEn deja de incluir las fechas posteriores al cierre")
    void cerrarEnLimitaLaVigencia() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        periodo.cerrarEn(LUNES_5_ENERO_2026.plusDays(13));

        assertFalse(periodo.estaAbierto());
        assertTrue(periodo.incluyeFecha(LUNES_5_ENERO_2026.plusDays(13)));
        assertFalse(periodo.incluyeFecha(LUNES_5_ENERO_2026.plusDays(14)));
    }

    @Test
    @DisplayName("no se puede cerrar antes de la fecha de inicio")
    void noSeCierraAntesDeEmpezar() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> periodo.cerrarEn(LUNES_5_ENERO_2026.minusDays(1))
        );
        assertEquals("PERIODO_RANGO_FECHAS_INVALIDO", ex.getCodigo());
    }

    @Test
    @DisplayName("atiende sólo los días configurados")
    void atiendeSoloLosDiasConfigurados() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        assertTrue(periodo.atiendeEnDia(DiaSemana.LUNES));
        assertTrue(periodo.atiendeEnDia(DiaSemana.MIERCOLES));
        assertFalse(periodo.atiendeEnDia(DiaSemana.MARTES));
        assertFalse(periodo.atiendeEnDia(null));
    }

    @Test
    @DisplayName("cubreRango exige que la cita quepa dentro de la franja de atención")
    void cubreRango() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        assertTrue(periodo.cubreRango(new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30))));
        assertTrue(periodo.cubreRango(new TimeRange(LocalTime.of(11, 30), LocalTime.of(12, 0))));
        assertFalse(periodo.cubreRango(new TimeRange(LocalTime.of(7, 30), LocalTime.of(8, 0))));
        assertFalse(periodo.cubreRango(new TimeRange(LocalTime.of(11, 45), LocalTime.of(12, 15))));
    }

    @Test
    @DisplayName("coincideConRejilla acepta sólo los inicios múltiplos del paso")
    void coincideConRejilla() {
        PeriodoDisponibilidad periodo = periodoAbierto();

        assertTrue(periodo.coincideConRejilla(new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30))));
        assertTrue(periodo.coincideConRejilla(new TimeRange(LocalTime.of(9, 30), LocalTime.of(10, 0))));
        // Empieza a mitad de slot
        assertFalse(periodo.coincideConRejilla(new TimeRange(LocalTime.of(8, 15), LocalTime.of(8, 45))));
        // Duración distinta a la configurada
        assertFalse(periodo.coincideConRejilla(new TimeRange(LocalTime.of(8, 0), LocalTime.of(9, 0))));
    }

    @Test
    @DisplayName("el paso de la rejilla suma la duración de la cita y el descanso")
    void pasoIncluyeDescanso() {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                LUNES_Y_MIERCOLES,
                new TimeRange(LocalTime.of(14, 0), LocalTime.of(18, 0)),
                45,
                15
        );

        assertEquals(60, periodo.getPasoMinutos());
        assertTrue(periodo.coincideConRejilla(new TimeRange(LocalTime.of(15, 0), LocalTime.of(15, 45))));
        assertFalse(periodo.coincideConRejilla(new TimeRange(LocalTime.of(14, 45), LocalTime.of(15, 30))));
    }

    @Test
    @DisplayName("detecta el cruce de fechas con otro periodo")
    void detectaSolapamientoDeFechas() {
        PeriodoDisponibilidad cerrado = periodoAbierto();
        cerrado.cerrarEn(LUNES_5_ENERO_2026.plusDays(13));

        assertTrue(cerrado.solapaConRangoFechas(LUNES_5_ENERO_2026.plusDays(10), null));
        assertFalse(cerrado.solapaConRangoFechas(LUNES_5_ENERO_2026.plusDays(14), null));
    }

    @Test
    @DisplayName("rechaza un descanso entre citas fuera de rango")
    void rechazaDescansoInvalido() {
        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> new PeriodoDisponibilidad(
                7L,
                LUNES_5_ENERO_2026,
                null,
                LUNES_Y_MIERCOLES,
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                30,
                999
        ));
        assertEquals("DESCANSO_ENTRE_CITAS_INVALIDO", ex.getCodigo());
    }
}
