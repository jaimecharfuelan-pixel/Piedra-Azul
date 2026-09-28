package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarCitasPorMedicoServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 29);

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;

    private ListarCitasPorMedicoService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ListarCitasPorMedicoService(
                citaRepository,
                catalogoMedicosPort,
                new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort)
        );
    }

    @Test
    void resumeCantidadesPorEstado() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Juan", "300")));
        when(catalogoPacientesPort.buscarResumenPaciente(3L))
                .thenReturn(Optional.of(new PacienteResumenDTO(3L, "María", "301")));
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA, OrdenCitas.HORA_ASC)).thenReturn(List.of(
                cita(1L, 2L, EstadoCita.PROGRAMADA),
                cita(2L, 3L, EstadoCita.ATENDIDA),
                cita(3L, 2L, EstadoCita.CANCELADA)
        ));

        var listado = servicio.ejecutar(1L, FECHA);

        assertEquals(3, listado.cantidad());
        assertEquals(1, listado.cantidadProgramadas());
        assertEquals(1, listado.cantidadAtendidas());
        assertEquals(1, listado.cantidadCanceladas());
        assertEquals("HORA_ASC", listado.orden());
    }

    @Test
    void ordenaPorNombreDePacienteCuandoElCriterioLoPide() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Zoe", "300")));
        when(catalogoPacientesPort.buscarResumenPaciente(3L))
                .thenReturn(Optional.of(new PacienteResumenDTO(3L, "Ana", "301")));
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA, OrdenCitas.PACIENTE_ASC)).thenReturn(List.of(
                cita(1L, 2L, EstadoCita.PROGRAMADA),
                cita(2L, 3L, EstadoCita.PROGRAMADA)
        ));

        var listado = servicio.ejecutar(1L, FECHA, OrdenCitas.PACIENTE_ASC);

        assertEquals("Ana", listado.citas().get(0).pacienteNombre());
        assertEquals("Zoe", listado.citas().get(1).pacienteNombre());
    }

    @Test
    void unRangoInvertidoSeRechaza() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutarPorRango(1L, FECHA.plusDays(2), FECHA)
        );
        assertEquals("RANGO_FECHAS_INVALIDO", ex.getCodigo());
    }

    @Test
    void elRangoSeRecortaA62Dias() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(citaRepository.buscarPorMedicoEntreFechas(1L, FECHA, FECHA.plusDays(62))).thenReturn(List.of());

        servicio.ejecutarPorRango(1L, FECHA, FECHA.plusDays(90));

        verify(citaRepository).buscarPorMedicoEntreFechas(1L, FECHA, FECHA.plusDays(62));
    }

    private static Cita cita(Long id, Long pacienteId, EstadoCita estado) {
        return Cita.reconstituir(
                id, pacienteId, 1L, FECHA,
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30)),
                estado
        );
    }
}
