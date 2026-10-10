package com.piedraazul.citas.aplicacion.puertos.entrada;

import com.piedraazul.citas.infraestructura.dto.HistorialCitasPorCedulaResponseDTO;

/**
 * Historial de citas de un paciente a partir de su cédula (endpoint 2/2 del par sin JWT).
 */
public interface ConsultarCitasPorCedulaUseCase {

    HistorialCitasPorCedulaResponseDTO ejecutar(String cedula);
}
