package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.entrada.GestionarCitaUseCase;
import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.aplicacion.puertos.salida.ConsultaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.Consulta;
import com.piedraazul.citas.infraestructura.dto.AgendarCitaCommand;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ConsultaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ReagendarCitaCommand;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.ConsultarConfiguracionPort;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.PeriodoDisponibilidadRef;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * RF2 + ciclo de vida de la cita.
 *
 * <p>{@code agendar} y {@code reagendar} reutilizan las mismas validaciones:
 * paciente y médico válidos, fecha dentro de la ventana de agendamiento, hora
 * dentro del periodo de disponibilidad vigente y sin solapamiento con otra cita.</p>
 */
@Service
@Transactional
public class GestionarCitaService implements GestionarCitaUseCase {

    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final CatalogoMedicosPort catalogoMedicosPort;
    private final CatalogoPacientesPort catalogoPacientesPort;
    private final ConsultarConfiguracionPort consultarConfiguracionPort;
    private final CitaDtoAssembler assembler;
    private final Clock clock;

    public GestionarCitaService(
            CitaRepository citaRepository,
            ConsultaRepository consultaRepository,
            CatalogoMedicosPort catalogoMedicosPort,
            CatalogoPacientesPort catalogoPacientesPort,
            ConsultarConfiguracionPort consultarConfiguracionPort,
            CitaDtoAssembler assembler,
            Clock clock
    ) {
        this.citaRepository = citaRepository;
        this.consultaRepository = consultaRepository;
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.catalogoPacientesPort = catalogoPacientesPort;
        this.consultarConfiguracionPort = consultarConfiguracionPort;
        this.assembler = assembler;
        this.clock = clock;
    }

    @Override
    public CitaResponseDTO agendar(AgendarCitaCommand comando) {
        validarPacienteRegistrado(comando.pacienteId());
        validarMedicoActivo(comando.medicoId());

        TimeRange rango = new TimeRange(comando.horaInicio(), comando.horaFin());
        validarDentroDeVentana(comando.fecha());
        validarNoEsPasado(comando.fecha(), rango);
        validarDentroDePeriodoVigente(comando.medicoId(), comando.fecha(), rango);
        validarSinSolapamiento(comando.medicoId(), comando.fecha(), rango, null);

        Cita cita = Cita.crear(comando.pacienteId(), comando.medicoId(), comando.fecha(), rango);
        return assembler.toDto(citaRepository.guardar(cita));
    }

    @Override
    public CitaResponseDTO cancelar(Long citaId) {
        Cita cita = requerirCita(citaId);
        cita.cancelar();
        return assembler.toDto(citaRepository.guardar(cita));
    }

    @Override
    public CitaResponseDTO reagendar(Long citaId, ReagendarCitaCommand comando) {
        Cita cita = requerirCita(citaId);
        // Validar estado antes que reglas de agenda.
        if (!cita.estaProgramada()) {
            throw ReglaDeNegocioException.de(
                    "CITA_NO_MODIFICABLE",
                    "Solo se puede reagendar una cita programada; esta está " + cita.getEstado()
            );
        }

        TimeRange nuevoRango = new TimeRange(comando.horaInicio(), comando.horaFin());
        validarMedicoActivo(cita.getMedicoId());
        validarDentroDeVentana(comando.fecha());
        validarNoEsPasado(comando.fecha(), nuevoRango);
        validarDentroDePeriodoVigente(cita.getMedicoId(), comando.fecha(), nuevoRango);
        validarSinSolapamiento(cita.getMedicoId(), comando.fecha(), nuevoRango, cita.getId());

        cita.reagendar(comando.fecha(), nuevoRango);
        return assembler.toDto(citaRepository.guardar(cita));
    }

    @Override
    public ConsultaResponseDTO marcarAtendida(Long citaId, String observaciones) {
        Cita cita = requerirCita(citaId);
        cita.marcarComoAtendida();
        Cita atendida = citaRepository.guardar(cita);

        Consulta consulta = consultaRepository.buscarPorCitaId(citaId)
                .orElseGet(() -> consultaRepository.guardar(Consulta.desdeCita(atendida, observaciones)));
        return assembler.toDto(consulta);
    }

    @Override
    @Transactional(readOnly = true)
    public CitaResponseDTO buscarPorId(Long citaId) {
        return assembler.toDto(requerirCita(citaId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponseDTO> listarPorPaciente(Long pacienteId) {
        validarPacienteRegistrado(pacienteId);
        return assembler.toDtos(citaRepository.buscarPorPaciente(pacienteId));
    }

    private Cita requerirCita(Long citaId) {
        return citaRepository.buscarPorId(citaId)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "CITA_NO_ENCONTRADA",
                        "No existe cita con id " + citaId
                ));
    }

    /**
     * RF2: el paciente debe tener registro en el sistema para poder agendar.
     */
    private void validarPacienteRegistrado(Long pacienteId) {
        if (!catalogoPacientesPort.existePaciente(pacienteId)) {
            throw RecursoNoEncontradoException.de(
                    "PACIENTE_NO_ENCONTRADO",
                    "El paciente " + pacienteId + " no está registrado en el sistema"
            );
        }
    }

    private void validarMedicoActivo(Long medicoId) {
        if (!catalogoMedicosPort.existeMedicoActivo(medicoId)) {
            throw ReglaDeNegocioException.de(
                    "MEDICO_NO_DISPONIBLE",
                    "El médico no existe o está inactivo"
            );
        }
    }

    /** RF3: la cita debe caer dentro de las semanas habilitadas. */
    private void validarDentroDeVentana(LocalDate fecha) {
        LocalDate hoy = LocalDate.now(clock);
        int semanas = consultarConfiguracionPort.obtenerVentanaSemanas();
        LocalDate limite = hoy.plusWeeks(semanas);
        if (fecha.isBefore(hoy) || fecha.isAfter(limite)) {
            throw ReglaDeNegocioException.de(
                    "FUERA_DE_VENTANA_AGENDAMIENTO",
                    "Solo se puede agendar entre el " + hoy + " y el " + limite
                            + " (ventana de " + semanas + " semanas)"
            );
        }
    }

    private void validarNoEsPasado(LocalDate fecha, TimeRange rango) {
        LocalTime ahora = LocalTime.now(clock);
        if (fecha.isEqual(LocalDate.now(clock)) && !rango.getHoraInicio().isAfter(ahora)) {
            throw ReglaDeNegocioException.de(
                    "SLOT_NO_DISPONIBLE",
                    "Esa franja ya pasó; elige una hora posterior a las " + ahora.withSecond(0).withNano(0)
            );
        }
    }

    private void validarDentroDePeriodoVigente(Long medicoId, LocalDate fecha, TimeRange rango) {
        PeriodoDisponibilidadRef periodo = consultarConfiguracionPort.obtenerPeriodoVigente(medicoId, fecha);

        DiaSemana dia = DiaSemana.desde(fecha);
        if (!periodo.atiendeEnDia(dia)) {
            throw ReglaDeNegocioException.de(
                    "MEDICO_NO_DISPONIBLE",
                    "El médico no atiende los " + dia.etiqueta().toLowerCase()
            );
        }
        if (!periodo.cubreRango(rango)) {
            throw ReglaDeNegocioException.de(
                    "SLOT_NO_DISPONIBLE",
                    "La hora solicitada está fuera de la franja de atención ("
                            + periodo.getFranjaHoraria().getHoraInicio() + " a "
                            + periodo.getFranjaHoraria().getHoraFin() + ")"
            );
        }
        if (!periodo.coincideConRejilla(rango)) {
            throw ReglaDeNegocioException.de(
                    "SLOT_NO_DISPONIBLE",
                    "La cita debe durar " + periodo.getDuracionCitaMinutos()
                            + " minutos y empezar en una de las franjas ofrecidas por el sistema"
            );
        }
    }

    private void validarSinSolapamiento(Long medicoId, LocalDate fecha, TimeRange rango, Long citaIdExcluida) {
        boolean ocupado = citaIdExcluida == null
                ? citaRepository.existeSolapamiento(medicoId, fecha, rango)
                : citaRepository.existeSolapamiento(medicoId, fecha, rango, citaIdExcluida);
        if (ocupado) {
            throw ReglaDeNegocioException.de(
                    "SLOT_NO_DISPONIBLE",
                    "Ese horario acaba de ser tomado por otro paciente; elige otra franja"
            );
        }
    }
}
