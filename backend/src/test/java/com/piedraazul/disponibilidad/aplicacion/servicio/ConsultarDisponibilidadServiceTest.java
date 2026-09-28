package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.ConsultarCitasPort;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.CalculadorSlotsStrategy;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.dominio.SlotDisponible;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 28);

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private ConsultarCitasPort consultarCitasPort;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private CalculadorSlotsStrategy calculadorSlots;
    @Mock
    private Clock clock;
    @InjectMocks
    private ConsultarDisponibilidadService servicio;

    @Test
    void omiteLasFranjasDeHoyQueYaPasaron() {
        fijarAhora(LocalTime.of(15, 0));
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(disponibilidadRepository.obtenerConfiguracionSistema()).thenReturn(ConfiguracionSistema.porDefecto());
        when(disponibilidadRepository.buscarPeriodoVigente(1L, HOY)).thenReturn(Optional.of(periodo()));
        when(consultarCitasPort.obtenerRangosOcupados(1L, HOY)).thenReturn(List.of());
        when(calculadorSlots.calcular(any(), eq(HOY), any())).thenReturn(List.of(
                new SlotDisponible(HOY, new TimeRange(LocalTime.of(14, 0), LocalTime.of(14, 30))),
                new SlotDisponible(HOY, new TimeRange(LocalTime.of(16, 0), LocalTime.of(16, 30)))
        ));

        var slots = servicio.ejecutar(1L, HOY);

        assertEquals(1, slots.size());
        assertEquals(LocalTime.of(16, 0), slots.get(0).horaInicio());
    }

    @Test
    void rechazaUnMedicoInactivo() {
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(false);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar(1L, HOY)
        );
        assertEquals("MEDICO_NO_DISPONIBLE", ex.getCodigo());
    }

    @Test
    void rechazaUnaFechaFueraDeLaVentana() {
        fijarAhora(LocalTime.of(8, 0));
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(disponibilidadRepository.obtenerConfiguracionSistema()).thenReturn(ConfiguracionSistema.reconstituir(1L, 1));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar(1L, HOY.plusWeeks(3))
        );
        assertEquals("FUERA_DE_VENTANA_AGENDAMIENTO", ex.getCodigo());
    }

    @Test
    void unRangoInvertidoNoSeConsulta() {
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutarPorRango(1L, HOY.plusDays(2), HOY)
        );
        assertEquals("RANGO_FECHAS_INVALIDO", ex.getCodigo());
    }

    @Test
    void elRangoOmiteLosDiasSinHorario() {
        fijarAhora(LocalTime.of(8, 0));
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(disponibilidadRepository.obtenerConfiguracionSistema()).thenReturn(ConfiguracionSistema.porDefecto());
        when(disponibilidadRepository.buscarPeriodoVigente(eq(1L), any())).thenReturn(Optional.empty());

        assertTrue(servicio.ejecutarPorRango(1L, HOY, HOY.plusDays(1)).isEmpty());
    }

    private void fijarAhora(LocalTime hora) {
        when(clock.instant()).thenReturn(HOY.atTime(hora).toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private static PeriodoDisponibilidad periodo() {
        return PeriodoDisponibilidad.reconstituir(
                1L, 1L, HOY.minusDays(7), null,
                EnumSet.allOf(DiaSemana.class),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(18, 0)),
                30, 0
        );
    }
}
