package com.piedraazul.disponibilidad.aplicacion.servicio;

import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import com.piedraazul.disponibilidad.dominio.SlotDisponible;
import com.piedraazul.disponibilidad.infraestructura.dto.ConfiguracionSistemaResponseDTO;
import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;
import com.piedraazul.disponibilidad.infraestructura.dto.SlotDisponibleDTO;
import com.piedraazul.nucleo.dominio.DiaSemana;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Traducción dominio → DTO del módulo Disponibilidad.
 */
final class DisponibilidadMapper {

    private DisponibilidadMapper() {
    }

    static ConfiguracionSistemaResponseDTO toDto(ConfiguracionSistema config, LocalDate hoy) {
        return new ConfiguracionSistemaResponseDTO(
                config.getId(),
                config.getVentanaSemanas(),
                hoy,
                config.ultimaFechaAgendable(hoy)
        );
    }

    static PeriodoDisponibilidadResponseDTO toDto(PeriodoDisponibilidad periodo, String medicoNombre, LocalDate hoy) {
        List<DiaSemana> dias = periodo.getDiasAtencion().stream()
                .sorted(Comparator.comparingInt(DiaSemana::ordinal))
                .toList();
        return new PeriodoDisponibilidadResponseDTO(
                periodo.getId(),
                periodo.getMedicoId(),
                medicoNombre,
                periodo.getFechaInicio(),
                periodo.getFechaFin(),
                dias,
                periodo.getFranjaHoraria().getHoraInicio(),
                periodo.getFranjaHoraria().getHoraFin(),
                periodo.getDuracionCitaMinutos(),
                periodo.getDescansoEntreCitasMinutos(),
                periodo.incluyeFecha(hoy)
        );
    }

    static SlotDisponibleDTO toDto(SlotDisponible slot) {
        return new SlotDisponibleDTO(
                slot.fecha(),
                slot.rango().getHoraInicio(),
                slot.rango().getHoraFin()
        );
    }
}
