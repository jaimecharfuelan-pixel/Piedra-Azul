package com.piedraazul.infraestructura.seed;

import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarPeriodoDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarVentanaAgendamientoUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarPeriodosDisponibilidadUseCase;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.personas.aplicacion.puertos.entrada.ConsultarPacienteUseCase;
import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarEspecialidadUseCase;
import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarMedicoUseCase;
import com.piedraazul.personas.aplicacion.puertos.salida.RegistrarPersonaPort;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.CrearMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
import com.piedraazul.personas.infraestructura.dto.MedicoResponseDTO;
import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Carga datos mínimos para poder probar los RF1, RF2 y RF3 en cuanto arranca el
 * contenedor: especialidades, médicos, pacientes, la ventana de agendamiento,
 * un horario por médico y un par de citas de ejemplo.
 *
 * <p>Se ejecuta a través de los casos de uso (no escribiendo SQL) para que los
 * datos sembrados cumplan las mismas reglas de negocio que la aplicación. Es
 * idempotente: si ya hay datos no toca nada. Se apaga con
 * {@code app.seed.enabled=false}.</p>
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class SeedDatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDatosDemo.class);

    private static final int VENTANA_SEMANAS_DEMO = 4;

    private final GestionarEspecialidadUseCase especialidades;
    private final GestionarMedicoUseCase medicos;
    private final ConsultarPacienteUseCase pacientes;
    private final RegistrarPersonaPort registrarPersona;
    private final ConfigurarVentanaAgendamientoUseCase ventana;
    private final ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo;
    private final ConsultarPeriodosDisponibilidadUseCase consultarPeriodos;
    private final GestionarCitaUseCase citas;
    private final Clock clock;

    public SeedDatosDemo(
            GestionarEspecialidadUseCase especialidades,
            GestionarMedicoUseCase medicos,
            ConsultarPacienteUseCase pacientes,
            RegistrarPersonaPort registrarPersona,
            ConfigurarVentanaAgendamientoUseCase ventana,
            ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo,
            ConsultarPeriodosDisponibilidadUseCase consultarPeriodos,
            GestionarCitaUseCase citas,
            Clock clock
    ) {
        this.especialidades = especialidades;
        this.medicos = medicos;
        this.pacientes = pacientes;
        this.registrarPersona = registrarPersona;
        this.ventana = ventana;
        this.configurarPeriodo = configurarPeriodo;
        this.consultarPeriodos = consultarPeriodos;
        this.citas = citas;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            sembrar();
        } catch (RuntimeException ex) {
            // Un problema con los datos de ejemplo no debe tumbar la aplicación.
            log.warn("No se pudieron sembrar los datos de demostración: {}", ex.getMessage());
        }
    }

    private void sembrar() {
        List<MedicoResponseDTO> existentes = medicos.listarActivos();
        if (!existentes.isEmpty()) {
            log.info("Datos de demostración ya presentes ({} médicos), no se siembra nada", existentes.size());
            return;
        }

        Long medicinaGeneral = crearEspecialidad("Medicina General");
        Long fisioterapia = crearEspecialidad("Fisioterapia");

        MedicoResponseDTO ana = medicos.crear(new CrearMedicoCommand("Dra. Ana Pérez", medicinaGeneral));
        MedicoResponseDTO carlos = medicos.crear(new CrearMedicoCommand("Ft. Carlos Muñoz", fisioterapia));

        Long juan = crearPaciente(1001L, "Juan Ramírez", "3001234567");
        crearPaciente(1002L, "María Gómez", "3009876543");

        ventana.ejecutar(VENTANA_SEMANAS_DEMO);

        LocalDate hoy = LocalDate.now(clock);
        configurarHorario(
                ana.id(),
                hoy,
                EnumSet.of(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                        DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                30,
                0
        );
        configurarHorario(
                carlos.id(),
                hoy,
                EnumSet.allOf(DiaSemana.class),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                45,
                15
        );

        agendarCitasDeEjemplo(ana.id(), juan, hoy);

        log.info("Datos de demostración creados: médicos {} y {}, ventana de {} semanas",
                ana.nombreCompleto(), carlos.nombreCompleto(), VENTANA_SEMANAS_DEMO);
    }

    private Long crearEspecialidad(String nombre) {
        return especialidades.listarActivas().stream()
                .filter(especialidad -> especialidad.nombre().equalsIgnoreCase(nombre))
                .map(EspecialidadResponseDTO::id)
                .findFirst()
                .orElseGet(() -> especialidades.crear(new CrearEspecialidadCommand(nombre)).id());
    }

    private Long crearPaciente(Long usuarioId, String nombre, String telefono) {
        return pacientes.listarTodos().stream()
                .filter(paciente -> paciente.nombreCompleto().equalsIgnoreCase(nombre))
                .map(PacienteResponseDTO::id)
                .findFirst()
                .orElseGet(() -> registrarPersona.crearPersonaParaUsuario(
                        RolUsuario.PACIENTE,
                        new DatosPersonaDTO(usuarioId, nombre, telefono, null)
                ));
    }

    private void configurarHorario(
            Long medicoId,
            LocalDate desde,
            Set<DiaSemana> dias,
            LocalTime horaInicio,
            LocalTime horaFin,
            int duracionMinutos,
            int descansoMinutos
    ) {
        if (!consultarPeriodos.porMedico(medicoId).isEmpty()) {
            return;
        }
        configurarPeriodo.ejecutar(new ConfigurarPeriodoCommand(
                medicoId,
                desde,
                null,
                dias,
                horaInicio,
                horaFin,
                duracionMinutos,
                descansoMinutos
        ));
    }

    /**
     * Agenda dos citas en el primer día hábil siguiente para que la tabla del RF1
     * no arranque vacía. Si alguna franja no aplica se omite en silencio.
     */
    private void agendarCitasDeEjemplo(Long medicoId, Long pacienteId, LocalDate hoy) {
        LocalDate fecha = hoy.plusDays(1);
        for (int intento = 0; intento < 7; intento++) {
            if (DiaSemana.desde(fecha) != DiaSemana.DOMINGO) {
                break;
            }
            fecha = fecha.plusDays(1);
        }
        agendarSiSePuede(medicoId, pacienteId, fecha, LocalTime.of(8, 0), LocalTime.of(8, 30));
        agendarSiSePuede(medicoId, pacienteId, fecha, LocalTime.of(9, 30), LocalTime.of(10, 0));
    }

    private void agendarSiSePuede(Long medicoId, Long pacienteId, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        try {
            citas.agendar(new AgendarCitaCommand(pacienteId, medicoId, fecha, inicio, fin));
        } catch (RuntimeException ex) {
            log.debug("Cita de ejemplo {} {}-{} no creada: {}", fecha, inicio, fin, ex.getMessage());
        }
    }
}
