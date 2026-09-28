package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitasModuleFacadeTest {

    @Mock
    private CitaRepository citaRepository;
    @InjectMocks
    private CitasModuleFacade fachada;

    @Test
    void devuelveSoloLosRangosDeLasCitasProgramadasQueEntregaElRepositorio() {
        LocalDate fecha = LocalDate.of(2026, 9, 29);
        TimeRange rango = new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30));
        when(citaRepository.buscarProgramadasPorMedicoYFecha(1L, fecha)).thenReturn(List.of(
                Cita.reconstituir(4L, 2L, 1L, fecha, rango, EstadoCita.PROGRAMADA)
        ));

        var ocupados = fachada.obtenerRangosOcupados(1L, fecha);

        assertEquals(List.of(rango), ocupados);
    }
}
