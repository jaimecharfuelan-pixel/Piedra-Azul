package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.infraestructura.dto.HistorialCitasPorCedulaResponseDTO;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarCitasPorCedulaServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 29);

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;

    private ConsultarCitasPorCedulaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ConsultarCitasPorCedulaService(
                citaRepository,
                catalogoPacientesPort,
                new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort)
        );
    }

    @Test
    void devuelveHistorialCuandoExistePacientePorCedula() {
        when(catalogoPacientesPort.buscarIdPorCedula("1001001001")).thenReturn(Optional.of(2L));
        when(catalogoPacientesPort.obtenerResumenPaciente(2L))
                .thenReturn(new PacienteResumenDTO(2L, "1001001001", "Juan Ramírez", "3001234567"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "1001001001", "Juan Ramírez", "3001234567")));
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(citaRepository.buscarPorPaciente(2L)).thenReturn(List.of(
                Cita.reconstituir(
                        10L, 2L, 1L, FECHA,
                        new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30)),
                        EstadoCita.PROGRAMADA
                )
        ));

        HistorialCitasPorCedulaResponseDTO historial = servicio.ejecutar("1001001001");

        assertEquals("1001001001", historial.cedula());
        assertEquals(2L, historial.pacienteId());
        assertEquals("Juan Ramírez", historial.pacienteNombre());
        assertEquals(1, historial.cantidad());
        assertEquals(1, historial.citas().size());
        assertEquals("Ana", historial.citas().get(0).medicoNombre());
    }

    @Test
    void lanzaPacienteNoEncontradoSiLaCedulaNoExiste() {
        when(catalogoPacientesPort.buscarIdPorCedula("9999999999")).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.ejecutar("9999999999")
        );

        assertEquals("PACIENTE_NO_ENCONTRADO", ex.getCodigo());
    }

    @Test
    void cantidadCeroCuandoElPacienteNoTieneCitas() {
        when(catalogoPacientesPort.buscarIdPorCedula("1003003003")).thenReturn(Optional.of(5L));
        when(catalogoPacientesPort.obtenerResumenPaciente(5L))
                .thenReturn(new PacienteResumenDTO(5L, "1003003003", "Pedro Vargas", "310"));
        when(citaRepository.buscarPorPaciente(5L)).thenReturn(List.of());

        HistorialCitasPorCedulaResponseDTO historial = servicio.ejecutar("1003003003");

        assertEquals(0, historial.cantidad());
        assertTrue(historial.citas().isEmpty());
    }
}
