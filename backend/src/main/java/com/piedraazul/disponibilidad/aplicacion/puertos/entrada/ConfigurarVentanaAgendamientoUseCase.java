package com.piedraazul.disponibilidad.aplicacion.puertos.entrada;

import com.piedraazul.disponibilidad.infraestructura.dto.ConfiguracionSistemaResponseDTO;

/**
 * RF3: el administrador define cuántas semanas a futuro se habilitan las citas.
 */
public interface ConfigurarVentanaAgendamientoUseCase {

    ConfiguracionSistemaResponseDTO ejecutar(int semanas);

    /** Lectura de la configuración actual para precargar el formulario. */
    ConfiguracionSistemaResponseDTO consultar();
}
