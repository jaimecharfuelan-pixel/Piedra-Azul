package com.piedraazul.disponibilidad.aplicacion.puertos.entrada;

import com.piedraazul.disponibilidad.infraestructura.dto.PeriodoDisponibilidadResponseDTO;

import java.util.List;

/**
 * RF3 (lectura): muestra el historial de horarios de un médico para que el
 * administrador vea qué está vigente antes de cambiarlo.
 */
public interface ConsultarPeriodosDisponibilidadUseCase {

    List<PeriodoDisponibilidadResponseDTO> porMedico(Long medicoId);
}
