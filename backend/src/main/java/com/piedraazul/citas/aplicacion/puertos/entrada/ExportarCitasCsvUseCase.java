package com.piedraazul.citas.aplicacion.puertos.entrada;

import java.time.LocalDate;

/**
 * Exporta la agenda del día (médico + fecha) en formato CSV.
 */
public interface ExportarCitasCsvUseCase {

    String ejecutar(Long medicoId, LocalDate fecha);
}
