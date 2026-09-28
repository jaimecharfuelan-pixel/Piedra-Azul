package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadModuleFacadeTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @InjectMocks
    private DisponibilidadModuleFacade fachada;

    @Test
    void exponeLaVentanaConfigurada() {
        when(disponibilidadRepository.obtenerConfiguracionSistema())
                .thenReturn(ConfiguracionSistema.reconstituir(1L, 6));

        assertEquals(6, fachada.obtenerVentanaSemanas());
    }

    @Test
    void devuelveElPeriodoVigente() {
        LocalDate fecha = LocalDate.of(2026, 9, 28);
        when(disponibilidadRepository.buscarPeriodoVigente(1L, fecha)).thenReturn(Optional.of(
                PeriodoDisponibilidad.reconstituir(
                        4L, 1L, fecha, null, EnumSet.of(DiaSemana.LUNES),
                        new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)), 30, 0
                )
        ));

        assertEquals(30, fachada.obtenerPeriodoVigente(1L, fecha).getDuracionCitaMinutos());
    }

    @Test
    void fallaSiEseDiaNoHayHorario() {
        LocalDate fecha = LocalDate.of(2026, 9, 28);
        when(disponibilidadRepository.buscarPeriodoVigente(1L, fecha)).thenReturn(Optional.empty());

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> fachada.obtenerPeriodoVigente(1L, fecha)
        );
        assertEquals("MEDICO_NO_DISPONIBLE", ex.getCodigo());
    }
}
