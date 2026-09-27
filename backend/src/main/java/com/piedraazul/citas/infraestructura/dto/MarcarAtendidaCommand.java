package com.piedraazul.citas.infraestructura.dto;

import jakarta.validation.constraints.Size;

/**
 * Observaciones que el médico deja al cerrar la cita; quedan en el historial.
 */
public record MarcarAtendidaCommand(

        @Size(max = 2000, message = "Las observaciones no pueden pasar de 2000 caracteres")
        String observaciones
) {
}
