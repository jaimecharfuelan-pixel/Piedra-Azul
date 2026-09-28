package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigurarPeriodoDisponibilidadServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 28);

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private Clock clock;
    @InjectMocks
    private ConfigurarPeriodoDisponibilidadService servicio;

    @Test
    void creaElPrimerHorarioAbierto() {
        fijarHoy();
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(disponibilidadRepository.buscarPeriodoAbiertoAnterior(1L)).thenReturn(Optional.empty());
        when(disponibilidadRepository.listarPeriodosPorMedico(1L)).thenReturn(List.of());
        when(disponibilidadRepository.guardarPeriodo(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.ejecutar(comando(LocalDate.of(2026, 9, 28)));

        assertEquals(LocalDate.of(2026, 9, 28), dto.fechaInicio());
        assertNull(dto.fechaFin());
        assertEquals("Ana", dto.medicoNombre());
    }

    @Test
    void laMismaFechaDeInicioActualizaElHorarioAbierto() {
        fijarHoy();
        PeriodoDisponibilidad abierto = periodo(LocalDate.of(2026, 9, 1), null);
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(disponibilidadRepository.buscarPeriodoAbiertoAnterior(1L)).thenReturn(Optional.of(abierto));
        when(disponibilidadRepository.guardarPeriodo(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.ejecutar(comando(LocalDate.of(2026, 9, 1)));

        assertEquals(7L, dto.id());
        assertEquals(LocalTime.of(8, 0), dto.horaInicio());
        verify(disponibilidadRepository, times(1)).guardarPeriodo(any());
    }

    @Test
    void unaFechaPosteriorCierraElHorarioAnteriorElDiaPrevio() {
        fijarHoy();
        PeriodoDisponibilidad abierto = periodo(LocalDate.of(2026, 9, 1), null);
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(disponibilidadRepository.buscarPeriodoAbiertoAnterior(1L)).thenReturn(Optional.of(abierto));
        when(disponibilidadRepository.listarPeriodosPorMedico(1L)).thenReturn(List.of(abierto));
        when(disponibilidadRepository.guardarPeriodo(any())).thenAnswer(inv -> inv.getArgument(0));

        servicio.ejecutar(comando(LocalDate.of(2026, 10, 5)));

        assertEquals(LocalDate.of(2026, 10, 4), abierto.getFechaFin());
        ArgumentCaptor<PeriodoDisponibilidad> captor = ArgumentCaptor.forClass(PeriodoDisponibilidad.class);
        verify(disponibilidadRepository, times(2)).guardarPeriodo(captor.capture());
        PeriodoDisponibilidad nuevo = captor.getAllValues().get(1);
        assertEquals(LocalDate.of(2026, 10, 5), nuevo.getFechaInicio());
        assertNull(nuevo.getFechaFin());
    }

    @Test
    void rechazaUnaFechaAnteriorAlHorarioAbierto() {
        PeriodoDisponibilidad abierto = periodo(LocalDate.of(2026, 9, 10), null);
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(disponibilidadRepository.buscarPeriodoAbiertoAnterior(1L)).thenReturn(Optional.of(abierto));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar(comando(LocalDate.of(2026, 9, 1)))
        );
        assertEquals("PERIODO_SOLAPADO", ex.getCodigo());
    }

    @Test
    void rechazaUnMedicoInactivo() {
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(false);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar(comando(HOY))
        );
        assertEquals("MEDICO_NO_DISPONIBLE", ex.getCodigo());
    }

    private void fijarHoy() {
        when(clock.instant()).thenReturn(HOY.atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private static ConfigurarPeriodoCommand comando(LocalDate inicio) {
        return new ConfigurarPeriodoCommand(
                1L,
                inicio,
                null,
                EnumSet.of(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                30,
                0
        );
    }

    private static PeriodoDisponibilidad periodo(LocalDate inicio, LocalDate fin) {
        return PeriodoDisponibilidad.reconstituir(
                7L,
                1L,
                inicio,
                fin,
                EnumSet.of(DiaSemana.LUNES),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                30,
                0
        );
    }
}
