package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportarCitasCsvServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 9, 29);

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;

    private ExportarCitasCsvService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ExportarCitasCsvService(citaRepository, catalogoPacientesPort);
    }

    @Test
    void exportaCabeceraYFilasConDatosDelPaciente() {
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA)).thenReturn(List.of(
                cita(10L, 2L, LocalTime.of(8, 0), LocalTime.of(8, 30), EstadoCita.PROGRAMADA)
        ));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "1001001001", "Juan Ramírez", "3001234567")));

        String csv = servicio.ejecutar(1L, FECHA);

        assertTrue(csv.startsWith("horaInicio,horaFin,pacienteNombre,pacienteTelefono,pacienteCedula,estado\n"));
        assertTrue(csv.contains("\"08:00\",\"08:30\",\"Juan Ramírez\",\"3001234567\",\"1001001001\",\"PROGRAMADA\""));
    }

    @Test
    void usaFallbackCuandoNoHayResumenDelPaciente() {
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA)).thenReturn(List.of(
                cita(11L, 99L, LocalTime.of(9, 0), LocalTime.of(9, 30), EstadoCita.ATENDIDA)
        ));
        when(catalogoPacientesPort.buscarResumenPaciente(99L)).thenReturn(Optional.empty());

        String csv = servicio.ejecutar(1L, FECHA);

        assertTrue(csv.contains("\"Paciente #99\""));
        assertTrue(csv.contains("\"ATENDIDA\""));
    }

    @Test
    void escapaComillasEnCamposDelCsv() {
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA)).thenReturn(List.of(
                cita(12L, 3L, LocalTime.of(10, 0), LocalTime.of(10, 30), EstadoCita.PROGRAMADA)
        ));
        when(catalogoPacientesPort.buscarResumenPaciente(3L))
                .thenReturn(Optional.of(new PacienteResumenDTO(3L, "1002002002", "Ana \"La Pro\"", "301")));

        String csv = servicio.ejecutar(1L, FECHA);

        assertTrue(csv.contains("\"Ana \"\"La Pro\"\"\""));
    }

    @Test
    void sinCitasSoloDevuelveCabecera() {
        when(citaRepository.buscarPorMedicoYFecha(1L, FECHA)).thenReturn(List.of());

        String csv = servicio.ejecutar(1L, FECHA);

        assertEquals("horaInicio,horaFin,pacienteNombre,pacienteTelefono,pacienteCedula,estado\n", csv);
    }

    private static Cita cita(Long id, Long pacienteId, LocalTime inicio, LocalTime fin, EstadoCita estado) {
        return Cita.reconstituir(
                id, pacienteId, 1L, FECHA,
                new TimeRange(inicio, fin),
                estado
        );
    }
}
