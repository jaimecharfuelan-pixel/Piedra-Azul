package com.piedraazul.nucleo.infraestructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj de la aplicación en zona {@code America/Bogota}.
 * Inyectable para fijar la fecha en pruebas con {@code Clock.fixed(...)}.
 */
@Configuration
public class RelojConfig {

    static final ZoneId ZONA_CLINICA = ZoneId.of("America/Bogota");

    @Bean
    Clock clock() {
        return Clock.system(ZONA_CLINICA);
    }
}
