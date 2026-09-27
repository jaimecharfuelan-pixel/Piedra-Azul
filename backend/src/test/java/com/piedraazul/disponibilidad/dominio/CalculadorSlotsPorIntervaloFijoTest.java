package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadorSlotsPorIntervaloFijoTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 1, 5);
    private static final LocalDate MARTES = LocalDate.of(2026, 1, 6);

    private final CalculadorSlotsStrategy calculador = new CalculadorSlotsPorIntervaloFijo();

    private static PeriodoDisponibilidad periodo(
            Set<DiaSemana> dias,
            LocalTime inicio,
            LocalTime fin,
            int duracion,
            int descanso
    ) {
        return new PeriodoDisponibilidad(1L, LUNES, null, dias, new TimeRange(inicio, fin), duracion, descanso);
    }

    @Test
    @DisplayName("parte la franja en slots del tamaño de la cita")
    void generaSlotsDeLaFranja() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(8, 0), LocalTime.of(10, 0), 30, 0
        );

        List<SlotDisponible> slots = calculador.calcular(periodo, LUNES, List.of());

        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(8, 0), slots.get(0).rango().getHoraInicio());
        assertEquals(LocalTime.of(9, 30), slots.get(3).rango().getHoraInicio());
        assertEquals(LocalTime.of(10, 0), slots.get(3).rango().getHoraFin());
        assertTrue(slots.stream().allMatch(slot -> slot.fecha().equals(LUNES)));
    }

    @Test
    @DisplayName("el descanso entre citas separa los slots y reduce su cantidad")
    void aplicaDescansoEntreCitas() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(14, 0), LocalTime.of(18, 0), 45, 15
        );

        List<SlotDisponible> slots = calculador.calcular(periodo, LUNES, List.of());

        assertEquals(4, slots.size());
        assertEquals(LocalTime.of(14, 0), slots.get(0).rango().getHoraInicio());
        assertEquals(LocalTime.of(14, 45), slots.get(0).rango().getHoraFin());
        assertEquals(LocalTime.of(15, 0), slots.get(1).rango().getHoraInicio());
        assertEquals(LocalTime.of(17, 45), slots.get(3).rango().getHoraFin());
    }

    @Test
    @DisplayName("descarta los slots que se cruzan con una cita ya programada")
    void descartaSlotsOcupados() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(8, 0), LocalTime.of(10, 0), 30, 0
        );
        List<TimeRange> ocupados = List.of(new TimeRange(LocalTime.of(9, 0), LocalTime.of(9, 30)));

        List<SlotDisponible> slots = calculador.calcular(periodo, LUNES, ocupados);

        assertEquals(3, slots.size());
        assertFalse(slots.stream().anyMatch(slot -> slot.rango().getHoraInicio().equals(LocalTime.of(9, 0))));
    }

    @Test
    @DisplayName("no ofrece nada un día que el médico no atiende")
    void sinSlotsSiNoAtiendeEseDia() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(8, 0), LocalTime.of(10, 0), 30, 0
        );

        assertTrue(calculador.calcular(periodo, MARTES, List.of()).isEmpty());
    }

    @Test
    @DisplayName("no ofrece nada en una fecha fuera de la vigencia del periodo")
    void sinSlotsFueraDeVigencia() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(8, 0), LocalTime.of(10, 0), 30, 0
        );
        periodo.cerrarEn(LUNES);

        assertTrue(calculador.calcular(periodo, LUNES.plusWeeks(1), List.of()).isEmpty());
    }

    @Test
    @DisplayName("una franja al final del día no genera slots dando la vuelta a medianoche")
    void noDesbordaMedianoche() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(22, 30), LocalTime.of(23, 30), 30, 0
        );

        List<SlotDisponible> slots = calculador.calcular(periodo, LUNES, List.of());

        assertEquals(2, slots.size());
        assertEquals(LocalTime.of(23, 30), slots.get(1).rango().getHoraFin());
    }

    @Test
    @DisplayName("una franja que no alcanza para una cita completa no genera slots")
    void franjaCortaNoGeneraSlots() {
        PeriodoDisponibilidad periodo = periodo(
                EnumSet.of(DiaSemana.LUNES), LocalTime.of(8, 0), LocalTime.of(8, 40), 40, 0
        );

        List<SlotDisponible> slots = calculador.calcular(periodo, LUNES, List.of());

        assertEquals(1, slots.size());
    }
}
