package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurarVentanaAgendamientoServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 28);

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private Clock clock;
    @InjectMocks
    private ConfigurarVentanaAgendamientoService servicio;

    @Test
    void actualizaLaVentanaYCalculaElUltimoDia() {
        fijarHoy();
        when(disponibilidadRepository.obtenerConfiguracionSistema()).thenReturn(ConfiguracionSistema.porDefecto());
        when(disponibilidadRepository.guardarConfiguracionSistema(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.ejecutar(6);

        assertEquals(6, dto.ventanaSemanas());
        assertEquals(HOY, dto.agendamientoDesde());
        assertEquals(HOY.plusWeeks(6), dto.agendamientoHasta());
    }

    @Test
    void rechazaUnaVentanaFueraDeRango() {
        when(disponibilidadRepository.obtenerConfiguracionSistema()).thenReturn(ConfiguracionSistema.porDefecto());

        ReglaDeNegocioException ex = assertThrows(ReglaDeNegocioException.class, () -> servicio.ejecutar(0));
        assertEquals("VENTANA_AGENDAMIENTO_INVALIDA", ex.getCodigo());
    }

    @Test
    void consultarDevuelveLaConfiguracionVigente() {
        fijarHoy();
        when(disponibilidadRepository.obtenerConfiguracionSistema())
                .thenReturn(ConfiguracionSistema.reconstituir(1L, 2));

        var dto = servicio.consultar();

        assertEquals(2, dto.ventanaSemanas());
        assertEquals(HOY.plusWeeks(2), dto.agendamientoHasta());
    }

    private void fijarHoy() {
        when(clock.instant()).thenReturn(HOY.atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }
}
