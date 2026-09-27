package com.piedraazul.bootstrap;

import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
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
import com.piedraazul.personas.aplicacion.puertos.entrada.RegistrarPacienteUseCase;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
import com.piedraazul.personas.infraestructura.dto.RegistrarPacienteCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Datos de demostración al arrancar la aplicación.
 *
 * Vive en {@code bootstrap} (no en un módulo de negocio) porque orquesta
 * Identidad, Personas, Disponibilidad y Citas.
 *
 * Spring invoca {@link #run} vía {@link ApplicationRunner} cuando el contexto
 * está listo, si {@code app.seed.enabled=true} y no existe el usuario {@code admin}.
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
    private final RegistrarPacienteUseCase registrarPaciente;
    private final ConfigurarVentanaAgendamientoUseCase ventana;
    private final ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo;
    private final ConsultarPeriodosDisponibilidadUseCase consultarPeriodos;
    private final GestionarCitaUseCase citas;
    private final Clock clock;

    public SeedDatosDemo(
            GestionarUsuarioUseCase usuarios,
            UsuarioRepository usuarioRepository,
            GestionarEspecialidadUseCase especialidades,
            RegistrarPacienteUseCase registrarPaciente,
            ConfigurarVentanaAgendamientoUseCase ventana,
            ConfigurarPeriodoDisponibilidadUseCase configurarPeriodo,
            ConsultarPeriodosDisponibilidadUseCase consultarPeriodos,
            GestionarCitaUseCase citas,
            Clock clock
    ) {
        this.usuarios = usuarios;
        this.usuarioRepository = usuarioRepository;
        this.especialidades = especialidades;
        this.registrarPaciente = registrarPaciente;
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
            log.warn("No se pudieron sembrar los datos de demostración: {}", ex.getMessage(), ex);
        }
    }

    private void sembrar() {
        if (usuarioRepository.existePorUsername("admin")) {
            log.info("Datos de demostración ya presentes, no se siembra nada");
            return;
        }

        Long medicinaGeneral = crearEspecialidad("Medicina General");
        Long fisioterapia = crearEspecialidad("Fisioterapia");
        crearEspecialidad("Odontología");

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
        UsuarioResponseDTO maria = crearUsuario(
                "maria.paciente",
                RolUsuario.PACIENTE,
                new DatosPersonaDTO(null, "María Gómez", "3009876543", null)
        );

        List<Long> pacientes = new ArrayList<>();
        pacientes.add(juan.personaId());
        pacientes.add(maria.personaId());
        pacientes.add(registrarWalkIn("Pedro Vargas", "3101112233"));
        pacientes.add(registrarWalkIn("Laura Méndez", "3102223344"));
        pacientes.add(registrarWalkIn("Andrés Soto", "3103334455"));
        pacientes.add(registrarWalkIn("Camila Ríos", "3104445566"));
        pacientes.add(registrarWalkIn("Diego Herrera", "3105556677"));
        pacientes.add(registrarWalkIn("Valentina Cruz", "3106667788"));

        ventana.ejecutar(VENTANA_SEMANAS_DEMO);

        LocalDate hoy = LocalDate.now(clock);
        // Horario desde hace una semana para que “hoy” ya esté cubierto
        LocalDate inicioHorario = hoy.minusWeeks(1);

        configurarHorario(
                ana.personaId(),
                inicioHorario,
                EnumSet.of(DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
                        DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO),
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                30,
                0
        );
        configurarHorario(
                carlos.personaId(),
                inicioHorario,
                EnumSet.allOf(DiaSemana.class),
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                45,
                15
        );

        int creadas = sembrarAgendaRica(ana.personaId(), carlos.personaId(), pacientes, hoy);

        log.info(
                "Demo lista ({} citas). Usuarios password {}: admin, agendador, ana.medico, carlos.medico, juan.paciente, maria.paciente",
                creadas,
                PASSWORD_DEMO
        );
    }

    /**
     * Agenda citas en la ventana futura; marca un subconjunto como atendidas
     * o canceladas para poblar calendario e historial.
     */
    private int sembrarAgendaRica(
            Long anaId,
            Long carlosId,
            List<Long> pacientes,
            LocalDate hoy
    ) {
        int creadas = 0;
        int idx = 0;
        List<Long> paraAtender = new ArrayList<>();
        List<Long> paraCancelar = new ArrayList<>();

        // Dra. Ana — mañanas (30 min): varios días laborables
        LocalTime[] slotsAna = {
                LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0),
                LocalTime.of(9, 30), LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30)
        };
        for (int diaOffset = 0; diaOffset <= 20; diaOffset++) {
            LocalDate fecha = hoy.plusDays(diaOffset);
            DiaSemana dia = DiaSemana.desde(fecha);
            if (dia == DiaSemana.DOMINGO) {
                continue;
            }
            int[] cuales = diaOffset % 3 == 0 ? new int[]{0, 2, 5} : new int[]{1, 4};
            for (int slotIdx : cuales) {
                LocalTime inicio = slotsAna[slotIdx];
                if (fecha.equals(hoy) && !inicio.isAfter(LocalTime.now(clock))) {
                    continue;
                }
                Long pacienteId = pacientes.get(idx++ % pacientes.size());
                CitaResponseDTO cita = agendarSiSePuede(
                        anaId, pacienteId, fecha, inicio, inicio.plusMinutes(30)
                );
                if (cita == null) {
                    continue;
                }
                creadas++;
                clasificarParaEstado(cita.id(), creadas, paraAtender, paraCancelar);
            }
        }

        // Ft. Carlos — tardes (45 + 15): todos los días
        LocalTime[] slotsCarlos = {
                LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        };
        for (int diaOffset = 0; diaOffset <= 14; diaOffset++) {
            LocalDate fecha = hoy.plusDays(diaOffset);
            int[] cuales = diaOffset % 2 == 0 ? new int[]{0, 2} : new int[]{1, 3};
            for (int slotIdx : cuales) {
                LocalTime inicio = slotsCarlos[slotIdx];
                if (fecha.equals(hoy) && !inicio.isAfter(LocalTime.now(clock))) {
                    continue;
                }
                Long pacienteId = pacientes.get(idx++ % pacientes.size());
                CitaResponseDTO cita = agendarSiSePuede(
                        carlosId, pacienteId, fecha, inicio, inicio.plusMinutes(45)
                );
                if (cita == null) {
                    continue;
                }
                creadas++;
                clasificarParaEstado(cita.id(), creadas, paraAtender, paraCancelar);
            }
        }

        for (Long citaId : paraAtender) {
            try {
                citas.marcarAtendida(citaId, "Consulta demo: paciente estable, control en 15 días.");
            } catch (RuntimeException ex) {
                log.debug("No se pudo marcar atendida {}: {}", citaId, ex.getMessage());
            }
        }
        for (Long citaId : paraCancelar) {
            try {
                citas.cancelar(citaId);
            } catch (RuntimeException ex) {
                log.debug("No se pudo cancelar {}: {}", citaId, ex.getMessage());
            }
        }

        return creadas;
    }

    private static void clasificarParaEstado(
            Long citaId,
            int creadas,
            List<Long> paraAtender,
            List<Long> paraCancelar
    ) {
        // ~8 atendidas (historial) y ~8 canceladas (colores en calendario)
        if (creadas <= 16 && creadas % 2 == 0) {
            paraAtender.add(citaId);
        } else if (creadas > 20 && creadas <= 40 && creadas % 3 == 0) {
            paraCancelar.add(citaId);
        }
    }

    private Long registrarWalkIn(String nombre, String telefono) {
        return registrarPaciente.ejecutar(new RegistrarPacienteCommand(nombre, telefono)).id();
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

    private CitaResponseDTO agendarSiSePuede(
            Long medicoId,
            Long pacienteId,
            LocalDate fecha,
            LocalTime inicio,
            LocalTime fin
    ) {
        try {
            return citas.agendar(new AgendarCitaCommand(pacienteId, medicoId, fecha, inicio, fin));
        } catch (RuntimeException ex) {
            log.debug("Cita de ejemplo {} {}-{} no creada: {}", fecha, inicio, fin, ex.getMessage());
            return null;
        }
    }
}
