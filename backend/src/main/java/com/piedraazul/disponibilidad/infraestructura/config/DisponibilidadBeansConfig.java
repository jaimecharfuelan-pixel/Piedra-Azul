package com.piedraazul.disponibilidad.infraestructura.config;

import com.piedraazul.disponibilidad.dominio.CalculadorSlotsPorIntervaloFijo;
import com.piedraazul.disponibilidad.dominio.CalculadorSlotsStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publica la estrategia de cálculo de slots como bean.
 *
 * <p>El registro vive en infraestructura para que las clases de
 * {@code dominio} queden libres de anotaciones de Spring; cambiar la regla de
 * cálculo es sustituir este bean.</p>
 */
@Configuration
public class DisponibilidadBeansConfig {

    @Bean
    CalculadorSlotsStrategy calculadorSlotsStrategy() {
        return new CalculadorSlotsPorIntervaloFijo();
    }
}
