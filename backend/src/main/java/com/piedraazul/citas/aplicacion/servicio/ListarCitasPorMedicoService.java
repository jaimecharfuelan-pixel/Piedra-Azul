package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.entrada.ListarCitasPorMedicoUseCase;
import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ListadoCitasResponseDTO;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * RF1: listado de citas de un médico en una fecha, con la cantidad total y el
 * desglose por estado.
 */
@Service
@Transactional(readOnly = true)
public class ListarCitasPorMedicoService implements ListarCitasPorMedicoUseCase {

    /** Dos meses: el mismo tope que usa la consulta de disponibilidad por rango. */
    private static final int MAXIMO_DIAS_POR_CONSULTA = 62;

    private final CitaRepository citaRepository;
    private final CatalogoMedicosPort catalogoMedicosPort;
    private final CitaDtoAssembler assembler;

    public ListarCitasPorMedicoService(
            CitaRepository citaRepository,
            CatalogoMedicosPort catalogoMedicosPort,
            CitaDtoAssembler assembler
    ) {
        this.citaRepository = citaRepository;
        this.catalogoMedicosPort = catalogoMedicosPort;
        this.assembler = assembler;
    }

    @Override
    public ListadoCitasResponseDTO ejecutar(Long medicoId, LocalDate fecha) {
        return ejecutar(medicoId, fecha, OrdenCitas.POR_DEFECTO);
    }

    @Override
    public ListadoCitasResponseDTO ejecutar(Long medicoId, LocalDate fecha, OrdenCitas orden) {
        String medicoNombre = catalogoMedicosPort.obtenerResumen(medicoId).nombreCompleto();
        OrdenCitas criterio = orden == null ? OrdenCitas.POR_DEFECTO : orden;

        List<Cita> citas = citaRepository.buscarPorMedicoYFecha(medicoId, fecha, criterio);
        List<CitaResponseDTO> filas = ordenarPorPacienteSiAplica(assembler.toDtos(citas), criterio);

        return new ListadoCitasResponseDTO(
                medicoId,
                medicoNombre,
                fecha,
                criterio.name(),
                filas.size(),
                contar(filas, EstadoCita.PROGRAMADA),
                contar(filas, EstadoCita.CANCELADA),
                contar(filas, EstadoCita.ATENDIDA),
                filas
        );
    }

    @Override
    public List<CitaResponseDTO> ejecutarPorRango(Long medicoId, LocalDate desde, LocalDate hasta) {
        catalogoMedicosPort.obtenerResumen(medicoId);
        if (hasta.isBefore(desde)) {
            throw ReglaDeNegocioException.de(
                    "RANGO_FECHAS_INVALIDO",
                    "La fecha final no puede ser anterior a la inicial"
            );
        }
        LocalDate tope = desde.plusDays(MAXIMO_DIAS_POR_CONSULTA);
        LocalDate fin = hasta.isAfter(tope) ? tope : hasta;
        return assembler.toDtos(citaRepository.buscarPorMedicoEntreFechas(medicoId, desde, fin));
    }

    /**
     * El nombre del paciente vive en el módulo Personas, así que ese orden se
     * aplica una vez resueltos los nombres. El resto de criterios ya vienen
     * ordenados por la base de datos.
     */
    private static List<CitaResponseDTO> ordenarPorPacienteSiAplica(List<CitaResponseDTO> filas, OrdenCitas orden) {
        if (!orden.ordenaPorPaciente()) {
            return filas;
        }
        Collator collator = Collator.getInstance(Locale.forLanguageTag("es-CO"));
        collator.setStrength(Collator.SECONDARY);
        Comparator<CitaResponseDTO> porNombre = Comparator.comparing(CitaResponseDTO::pacienteNombre, collator);
        return filas.stream()
                .sorted(orden.esDescendente() ? porNombre.reversed() : porNombre)
                .toList();
    }

    private static int contar(List<CitaResponseDTO> filas, EstadoCita estado) {
        return (int) filas.stream().filter(fila -> fila.estado() == estado).count();
    }
}
