package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.EstadoVigenciaPeriodo;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.dominio.SlotDisponible;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DisponibilidadMapperTest {

    @Test
    void traduceLaVentanaALimitesDeFecha() {
        LocalDate hoy = LocalDate.of(2026, 9, 28);

        var dto = DisponibilidadMapper.toDto(ConfiguracionSistema.reconstituir(1L, 2), hoy);

        assertEquals(hoy, dto.agendamientoDesde());
        assertEquals(hoy.plusWeeks(2), dto.agendamientoHasta());
    }

    @Test
    void unInicioFuturoNoQuedaVigente() {
        LocalDate hoy = LocalDate.of(2026, 9, 28);
        PeriodoDisponibilidad futuro = PeriodoDisponibilidad.reconstituir(
                1L, 1L, hoy.plusDays(1), null, EnumSet.of(DiaSemana.MARTES, DiaSemana.LUNES),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)), 30, 0
        );

        var dto = DisponibilidadMapper.toDto(futuro, "Ana", hoy);

        assertEquals(EstadoVigenciaPeriodo.FUTURO, dto.estado());
        assertFalse(dto.vigente());
        assertEquals(DiaSemana.LUNES, dto.diasAtencion().get(0));
        assertEquals(DiaSemana.MARTES, dto.diasAtencion().get(1));
    }

    @Test
    void traduceUnSlot() {
        SlotDisponible slot = new SlotDisponible(
                LocalDate.of(2026, 9, 28),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30))
        );

        var dto = DisponibilidadMapper.toDto(slot);

        assertEquals(LocalTime.of(8, 30), dto.horaFin());
    }
}
