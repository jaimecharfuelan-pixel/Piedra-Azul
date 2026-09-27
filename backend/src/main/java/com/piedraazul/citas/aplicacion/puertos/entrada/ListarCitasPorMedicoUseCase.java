package com.piedraazul.citas.aplicacion.puertos.entrada;

import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.citas.infraestructura.dto.CitaResponseDTO;
import com.piedraazul.citas.infraestructura.dto.ListadoCitasResponseDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * RF1: listar las citas de un médico/terapista en una fecha determinada,
 * con la cantidad y permitiendo cambiar el orden de la tabla.
 */
public interface ListarCitasPorMedicoUseCase {

    ListadoCitasResponseDTO ejecutar(Long medicoId, LocalDate fecha);

    ListadoCitasResponseDTO ejecutar(Long medicoId, LocalDate fecha, OrdenCitas orden);

    /** Vista semanal/mensual del calendario del médico. */
    List<CitaResponseDTO> ejecutarPorRango(Long medicoId, LocalDate desde, LocalDate hasta);
}
