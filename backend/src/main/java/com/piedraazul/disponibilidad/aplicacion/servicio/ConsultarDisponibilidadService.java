package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.ConsultarCitasPort;
import com.piedraazul.disponibilidad.aplicacion.puertos.entrada.ConsultarDisponibilidadUseCase;
import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.CalculadorSlotsStrategy;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.dominio.SlotDisponible;
import com.piedraazul.disponibilidad.infraestructura.dto.SlotDisponibleDTO;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RF2: franjas libres de un médico, ya descontadas las citas programadas.
 */
@Service
@Transactional(readOnly = true)
public class ConsultarDisponibilidadService implements ConsultarDisponibilidadUseCase {

    /** Tope defensivo para la consulta por rango (dos meses de calendario). */
    private static final int MAXIMO_DIAS_POR_CONSULTA = 62;

    private final DisponibilidadRepository disponibilidadRepository;
    private final ConsultarCitasPort consultarCitasPort;
    private final CatalogoMedicosPort catalogoMedicosPort;
    private final CalculadorSlotsStrategy calculadorSlots;
    private final Clock clock;

    public ConsultarDisponibilidadService(
            DisponibilidadRepository disponibilidadRepository,
            ConsultarCitasPort consultarCitasPort,
            CatalogoMedicosPort catalogoMedicosPort,
            CalculadorSlotsStrategy calculadorSlots,
            Clock clock
    ) {
        this.disponibilidadRepository = disponibilidadRepository;
        this.consultarCitasPort = consultarCitasPort;
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.calculadorSlots = calculadorSlots;
        this.clock = clock;
    }

    @Override
    public List<SlotDisponibleDTO> ejecutar(Long medicoId, LocalDate fecha) {
        validarMedicoActivo(medicoId);
        ConfiguracionSistema config = disponibilidadRepository.obtenerConfiguracionSistema();
        LocalDate hoy = LocalDate.now(clock);

        if (!config.permiteAgendarEn(fecha, hoy)) {
            throw ReglaDeNegocioException.de(
                    "FUERA_DE_VENTANA_AGENDAMIENTO",
                    "Solo se puede agendar entre el " + hoy + " y el " + config.ultimaFechaAgendable(hoy)
                            + " (ventana de " + config.getVentanaSemanas() + " semanas)"
            );
        }

        PeriodoDisponibilidad periodo = disponibilidadRepository.buscarPeriodoVigente(medicoId, fecha)
                .orElseThrow(() -> ReglaDeNegocioException.de(
                        "MEDICO_NO_DISPONIBLE",
                        "El médico no tiene horario de atención configurado para el " + fecha
                ));

        return calcularParaFecha(periodo, fecha, hoy);
    }

    @Override
    public List<SlotDisponibleDTO> ejecutarPorRango(Long medicoId, LocalDate desde, LocalDate hasta) {
        validarMedicoActivo(medicoId);
        if (hasta.isBefore(desde)) {
            throw ReglaDeNegocioException.de(
                    "RANGO_FECHAS_INVALIDO",
                    "La fecha final no puede ser anterior a la inicial"
            );
        }

        ConfiguracionSistema config = disponibilidadRepository.obtenerConfiguracionSistema();
        LocalDate hoy = LocalDate.now(clock);
        LocalDate tope = desde.plusDays(MAXIMO_DIAS_POR_CONSULTA);
        LocalDate fin = hasta.isAfter(tope) ? tope : hasta;

        List<SlotDisponibleDTO> resultado = new ArrayList<>();
        for (LocalDate fecha = desde; !fecha.isAfter(fin); fecha = fecha.plusDays(1)) {
            if (!config.permiteAgendarEn(fecha, hoy)) {
                continue;
            }
            // El calendario siempre pide semanas completas: los días sin horario
            // configurado simplemente no aportan franjas.
            LocalDate dia = fecha;
            disponibilidadRepository.buscarPeriodoVigente(medicoId, dia)
                    .ifPresent(periodo -> resultado.addAll(calcularParaFecha(periodo, dia, hoy)));
        }
        return List.copyOf(resultado);
    }

    private List<SlotDisponibleDTO> calcularParaFecha(PeriodoDisponibilidad periodo, LocalDate fecha, LocalDate hoy) {
        List<TimeRange> ocupados = consultarCitasPort.obtenerRangosOcupados(periodo.getMedicoId(), fecha);
        List<SlotDisponible> slots = calculadorSlots.calcular(periodo, fecha, ocupados);
        LocalTime ahora = LocalTime.now(clock);
        return slots.stream()
                .filter(slot -> !yaPaso(slot, fecha, hoy, ahora))
                .map(DisponibilidadMapper::toDto)
                .toList();
    }

    /** Hoy no se ofrecen franjas cuya hora de inicio ya pasó. */
    private static boolean yaPaso(SlotDisponible slot, LocalDate fecha, LocalDate hoy, LocalTime ahora) {
        return fecha.isEqual(hoy) && !slot.rango().getHoraInicio().isAfter(ahora);
    }

    private void validarMedicoActivo(Long medicoId) {
        if (!catalogoMedicosPort.existeMedicoActivo(medicoId)) {
            throw ReglaDeNegocioException.de(
                    "MEDICO_NO_DISPONIBLE",
                    "El médico no existe o está inactivo"
            );
        }
    }
}
