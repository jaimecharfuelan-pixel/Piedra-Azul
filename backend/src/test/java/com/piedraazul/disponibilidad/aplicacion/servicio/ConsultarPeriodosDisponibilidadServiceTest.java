package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.EstadoVigenciaPeriodo;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarPeriodosDisponibilidadServiceTest {

    @Mock
    private DisponibilidadRepository disponibilidadRepository;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private Clock clock;
    @InjectMocks
    private ConsultarPeriodosDisponibilidadService servicio;

    @Test
    void distingueVigenteFuturoEHistoricoRespectoAHoy() {
        LocalDate hoy = LocalDate.of(2026, 9, 28);
        when(clock.instant()).thenReturn(hoy.atStartOfDay().toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(disponibilidadRepository.listarPeriodosPorMedico(1L)).thenReturn(List.of(
                periodo(1L, hoy.plusDays(7), null),
                periodo(2L, hoy.minusDays(10), hoy.minusDays(1)),
                periodo(3L, hoy.minusDays(1), null)
        ));

        var periodos = servicio.porMedico(1L);

        assertEquals(EstadoVigenciaPeriodo.FUTURO, periodos.get(0).estado());
        assertEquals(EstadoVigenciaPeriodo.HISTORICO, periodos.get(1).estado());
        assertEquals(EstadoVigenciaPeriodo.VIGENTE, periodos.get(2).estado());
        assertEquals("Ana", periodos.get(2).medicoNombre());
    }

    private static PeriodoDisponibilidad periodo(Long id, LocalDate inicio, LocalDate fin) {
        return PeriodoDisponibilidad.reconstituir(
                id, 1L, inicio, fin, EnumSet.of(DiaSemana.LUNES),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)), 30, 0
        );
    }
}
