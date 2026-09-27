package com.piedraazul.disponibilidad.infraestructura.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Cuerpo de la petición que cambia la ventana de agendamiento en semanas (RF3).
 */
public record ActualizarVentanaCommand(

        @Min(value = 1, message = "La ventana debe ser de al menos 1 semana")
        @Max(value = 52, message = "La ventana no puede pasar de 52 semanas")
        int semanas
) {
}
