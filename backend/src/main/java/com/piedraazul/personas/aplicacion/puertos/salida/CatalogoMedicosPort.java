package com.piedraazul.personas.aplicacion.puertos.salida;

import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;

/**
 * Puerto público consumido por Disponibilidad y Citas.
 */
public interface CatalogoMedicosPort {
    boolean existeMedicoActivo(Long medicoId);

    MedicoResumenDTO obtenerResumen(Long medicoId);
}
