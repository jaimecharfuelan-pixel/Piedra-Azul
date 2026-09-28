package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.aplicacion.puertos.salida.ConsultaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.Consulta;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.ReagendarCitaCommand;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.ConsultarConfiguracionPort;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionarCitaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 9, 28);
    private static final LocalDate MANANA = LocalDate.of(2026, 9, 29);

    @Mock
    private CitaRepository citaRepository;
    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;
    @Mock
    private ConsultarConfiguracionPort consultarConfiguracionPort;

    private GestionarCitaService servicio;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(HOY.atTime(10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        servicio = new GestionarCitaService(
                citaRepository,
                consultaRepository,
                catalogoMedicosPort,
                catalogoPacientesPort,
                consultarConfiguracionPort,
                new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort),
                reloj
        );
    }

    @Test
    void agendaDentroDelHorarioVigente() {
        prepararAgendaValida();
        when(citaRepository.existeSolapamiento(eq(1L), eq(MANANA), any())).thenReturn(false);
        when(citaRepository.guardar(any())).thenAnswer(inv -> conId(4L, inv.getArgument(0)));
        nombres();

        var dto = servicio.agendar(new AgendarCitaCommand(2L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(8, 30)));

        assertEquals(4L, dto.id());
        assertEquals(EstadoCita.PROGRAMADA, dto.estado());
        assertEquals("Ana", dto.medicoNombre());
        assertEquals("Juan", dto.pacienteNombre());
    }

    @Test
    void noAgendaSiElMedicoEstaInactivo() {
        when(catalogoPacientesPort.existePaciente(2L)).thenReturn(true);
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(false);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.agendar(new AgendarCitaCommand(2L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(8, 30)))
        );
        assertEquals("MEDICO_NO_DISPONIBLE", ex.getCodigo());
    }

    @Test
    void noAgendaUnaFranjaYaOcupada() {
        prepararAgendaValida();
        when(citaRepository.existeSolapamiento(eq(1L), eq(MANANA), any())).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.agendar(new AgendarCitaCommand(2L, 1L, MANANA, LocalTime.of(8, 0), LocalTime.of(8, 30)))
        );
        assertEquals("SLOT_NO_DISPONIBLE", ex.getCodigo());
    }

    @Test
    void cancelaUnaCitaProgramada() {
        Cita cita = Cita.reconstituir(4L, 2L, 1L, MANANA, rango(), EstadoCita.PROGRAMADA);
        when(citaRepository.buscarPorId(4L)).thenReturn(Optional.of(cita));
        when(citaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        nombres();

        var dto = servicio.cancelar(4L);

        assertEquals(EstadoCita.CANCELADA, dto.estado());
    }

    @Test
    void noReagendaUnaCitaQueYaNoEstaProgramada() {
        when(citaRepository.buscarPorId(4L)).thenReturn(Optional.of(
                Cita.reconstituir(4L, 2L, 1L, MANANA, rango(), EstadoCita.ATENDIDA)
        ));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.reagendar(4L, new ReagendarCitaCommand(MANANA, LocalTime.of(9, 0), LocalTime.of(9, 30)))
        );
        assertEquals("CITA_NO_MODIFICABLE", ex.getCodigo());
    }

    @Test
    void marcarAtendidaCreaLaConsulta() {
        Cita cita = Cita.reconstituir(4L, 2L, 1L, MANANA, rango(), EstadoCita.PROGRAMADA);
        when(citaRepository.buscarPorId(4L)).thenReturn(Optional.of(cita));
        when(citaRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(consultaRepository.buscarPorCitaId(4L)).thenReturn(Optional.empty());
        when(consultaRepository.guardar(any())).thenAnswer(inv -> {
            Consulta consulta = inv.getArgument(0);
            return Consulta.reconstituir(
                    9L, consulta.getCitaId(), consulta.getMedicoId(), consulta.getPacienteId(),
                    consulta.getFecha(), consulta.getObservaciones(), LocalDateTime.of(2026, 9, 29, 11, 0)
            );
        });
        nombres();

        var dto = servicio.marcarAtendida(4L, "Control");

        assertEquals(9L, dto.id());
        assertEquals("Control", dto.observaciones());
        assertEquals(EstadoCita.ATENDIDA, cita.getEstado());
    }

    @Test
    void listarPorPacienteExigeQueExista() {
        when(catalogoPacientesPort.existePaciente(2L)).thenReturn(false);

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.listarPorPaciente(2L)
        );
        assertEquals("PACIENTE_NO_ENCONTRADO", ex.getCodigo());
    }

    @Test
    void buscarPorIdFallaSiNoHayCita() {
        when(citaRepository.buscarPorId(4L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.buscarPorId(4L)
        );
        assertEquals("CITA_NO_ENCONTRADA", ex.getCodigo());
    }

    private void prepararAgendaValida() {
        when(catalogoPacientesPort.existePaciente(2L)).thenReturn(true);
        when(catalogoMedicosPort.existeMedicoActivo(1L)).thenReturn(true);
        when(consultarConfiguracionPort.obtenerVentanaSemanas()).thenReturn(4);
        when(consultarConfiguracionPort.obtenerPeriodoVigente(1L, MANANA)).thenReturn(periodo());
    }

    private void nombres() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Juan", "300")));
    }

    private static Cita conId(Long id, Cita cita) {
        return Cita.reconstituir(id, cita.getPacienteId(), cita.getMedicoId(), cita.getFecha(), cita.getRango(), cita.getEstado());
    }

    private static TimeRange rango() {
        return new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30));
    }

    private static PeriodoDisponibilidad periodo() {
        return PeriodoDisponibilidad.reconstituir(
                1L, 1L, HOY.minusWeeks(1), null,
                EnumSet.of(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES, DiaSemana.JUEVES, DiaSemana.VIERNES),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)),
                30, 0
        );
    }
}
