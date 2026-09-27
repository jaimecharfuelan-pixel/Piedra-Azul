package com.piedraazul.identidad.infraestructura.seed;

import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarPeriodoDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarVentanaAgendamientoUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarPeriodosDisponibilidadUseCase;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.identidad.aplicacion.puertos.entrada.GestionarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarEspecialidadUseCase;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
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
import java.util.Set;

/**
 * Datos demo + usuarios de prueba (password: {@code demo1234}).
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = false)
public class SeedDatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDatosDemo.class);
    private static final String PASSWORD_DEMO = "demo1234";
    private static final int VENTANA_SEMANAS_DEMO = 4;

    private final GestionarUsuarioUseCase usuarios;
    private final UsuarioRepository usuarioRepository;
    private final GestionarEspecialidadUseCase especialidades;
    private final ConfigurarVentanaAgendamientoUseCase ventana;
    private final ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo;
    private final ConsultarPeriodosDisponibilidadUseCase consultarPeriodos;
    private final GestionarCitaUseCase citas;
    private final Clock clock;

    public SeedDatosDemo(
            GestionarUsuarioUseCase usuarios,
            UsuarioRepository usuarioRepository,
            GestionarEspecialidadUseCase especialidades,
            ConfigurarVentanaAgendamientoUseCase ventana,
            ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo,
            ConsultarPeriodosDisponibilidadUseCase consultarPeriodos,
            GestionarCitaUseCase citas,
            Clock clock
    ) {
        this.usuarios = usuarios;
        this.usuarioRepository = usuarioRepository;
        this.especialidades = especialidades;
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
            log.warn("No se pudieron sembrar los datos de demostración: {}", ex.getMessage());
        }
    }

    private void sembrar() {
        if (usuarioRepository.existePorUsername("admin")) {
            log.info("Datos de demostración ya presentes, no se siembra nada");
            return;
        }

        Long medicinaGeneral = crearEspecialidad("Medicina General");
        Long fisioterapia = crearEspecialidad("Fisioterapia");

        crearUsuario("admin", RolUsuario.ADMINISTRADOR, null);
        crearUsuario("agendador", RolUsuario.AGENDADOR, null);

        UsuarioResponseDTO ana = crearUsuario(
                "ana.medico",
                RolUsuario.MEDICO,
                new DatosPersonaDTO(null, "Dra. Ana Pérez", null, medicinaGeneral)
        );
        UsuarioResponseDTO carlos = crearUsuario(
                "carlos.medico",
                RolUsuario.MEDICO,
                new DatosPersonaDTO(null, "Ft. Carlos Muñoz", null, fisioterapia)
        );
        UsuarioResponseDTO juan = crearUsuario(
                "juan.paciente",
                RolUsuario.PACIENTE,
                new DatosPersonaDTO(null, "Juan Ramírez", "3001234567", null)
        );
        crearUsuario(
                "maria.paciente",
                RolUsuario.PACIENTE,
                new DatosPersonaDTO(null, "María Gómez", "3009876543", null)
        );

        ventana.ejecutar(VENTANA_SEMANAS_DEMO);

        LocalDate hoy = LocalDate.now(clock);
        configurarHorario(
                ana.personaId(),
                hoy,
                EnumSet.of(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                        DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                30,
                0
        );
        configurarHorario(
                carlos.personaId(),
                hoy,
                EnumSet.allOf(DiaSemana.class),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                45,
                15
        );

        agendarCitasDeEjemplo(ana.personaId(), juan.personaId(), hoy);

        log.info(
                "Demo lista. Usuarios (password {}): admin, agendador, ana.medico, carlos.medico, juan.paciente, maria.paciente",
                PASSWORD_DEMO
        );
    }

    private UsuarioResponseDTO crearUsuario(String username, RolUsuario rol, DatosPersonaDTO datos) {
        return usuarios.crear(new CrearUsuarioCommand(username, PASSWORD_DEMO, rol, datos));
    }

    private Long crearEspecialidad(String nombre) {
        return especialidades.listarActivas().stream()
                .filter(especialidad -> especialidad.nombre().equalsIgnoreCase(nombre))
                .map(EspecialidadResponseDTO::id)
                .findFirst()
                .orElseGet(() -> especialidades.crear(new CrearEspecialidadCommand(nombre)).id());
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
