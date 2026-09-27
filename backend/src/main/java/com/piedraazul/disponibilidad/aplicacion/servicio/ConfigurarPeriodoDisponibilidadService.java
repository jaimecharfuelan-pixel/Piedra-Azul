package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConfigurarPeriodoDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfigurarPeriodoCommand;
import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * RF3: registra el horario de atención de un médico (días, franja, duración de
 * la cita y descanso entre citas) a partir de una fecha.
 *
 * <p>Si el médico ya tenía un periodo abierto, se cierra el día anterior al
 * inicio del nuevo, de modo que nunca coexistan dos horarios del mismo médico.</p>
 */
@Service
@Transactional
public class ConfigurarPeriodoDisponibilidadService implements ConfigurarPeriodoDisponibilidadUseCase {

    private final DisponibilidadRepository disponibilidadRepository;
    private final CatalogoMedicosPort catalogoMedicosPort;
    private final Clock clock;

    public ConfigurarPeriodoDisponibilidadService(
            DisponibilidadRepository disponibilidadRepository,
            CatalogoMedicosPort catalogoMedicosPort,
            Clock clock
    ) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.clock = clock;
    }

    @Override
    public PeriodoDisponibilidadResponseDTO ejecutar(ConfigurarPeriodoCommand comando) {
        if (!catalogoMedicosPort.existeMedicoActivo(comando.medicoId())) {
            throw ReglaDeNegocioException.de(
                    "MEDICO_NO_DISPONIBLE",
                    "El médico no existe o está inactivo, no se le puede configurar horario"
            );
        }

        TimeRange franja = new TimeRange(comando.horaInicio(), comando.horaFin());

        PeriodoDisponibilidad abierto = disponibilidadRepository
                .buscarPeriodoAbiertoAnterior(comando.medicoId())
                .orElse(null);

        // Misma fecha de inicio: se actualiza el horario abierto en lugar de rechazarlo.
        if (abierto != null && abierto.getFechaInicio().isEqual(comando.fechaInicio())) {
            PeriodoDisponibilidad reemplazo = PeriodoDisponibilidad.reconstituir(
                    abierto.getId(),
                    comando.medicoId(),
                    comando.fechaInicio(),
                    comando.fechaFin(),
                    comando.diasAtencion(),
                    franja,
                    comando.duracionCitaMinutos(),
                    comando.descansoEnMinutos()
            );
            return toResponse(disponibilidadRepository.guardarPeriodo(reemplazo));
        }

        PeriodoDisponibilidad nuevo = new PeriodoDisponibilidad(
                comando.medicoId(),
                comando.fechaInicio(),
                comando.fechaFin(),
                comando.diasAtencion(),
                franja,
                comando.duracionCitaMinutos(),
                comando.descansoEnMinutos()
        );

        cerrarPeriodoAnteriorSiExiste(abierto, comando.fechaInicio());
        validarSinSolapamiento(nuevo);

        return toResponse(disponibilidadRepository.guardarPeriodo(nuevo));
    }

    private PeriodoDisponibilidadResponseDTO toResponse(PeriodoDisponibilidad guardado) {
        return DisponibilidadMapper.toDto(
                guardado,
                catalogoMedicosPort.obtenerResumen(guardado.getMedicoId()).nombreCompleto(),
                LocalDate.now(clock)
        );
    }

    private void cerrarPeriodoAnteriorSiExiste(PeriodoDisponibilidad anterior, LocalDate fechaInicio) {
        if (anterior == null) {
            return;
        }
        if (!anterior.getFechaInicio().isBefore(fechaInicio)) {
            throw ReglaDeNegocioException.de(
                    "PERIODO_SOLAPADO",
                    "El médico ya tiene un horario vigente desde el " + anterior.getFechaInicio()
                            + ". Elige esa misma fecha para actualizarlo, o una posterior para reemplazarlo."
            );
        }
        anterior.cerrarEn(fechaInicio.minusDays(1));
        disponibilidadRepository.guardarPeriodo(anterior);
    }

    private void validarSinSolapamiento(PeriodoDisponibilidad nuevo) {
        disponibilidadRepository.listarPeriodosPorMedico(nuevo.getMedicoId()).stream()
                .filter(existente -> existente.solapaConRangoFechas(nuevo.getFechaInicio(), nuevo.getFechaFin()))
                .findFirst()
                .ifPresent(existente -> {
                    throw ReglaDeNegocioException.de(
                            "PERIODO_SOLAPADO",
                            "El nuevo horario se cruza con el periodo del " + existente.getFechaInicio()
                                    + " al " + (existente.getFechaFin() == null ? "indefinido" : existente.getFechaFin())
                    );
                });
    }
}
