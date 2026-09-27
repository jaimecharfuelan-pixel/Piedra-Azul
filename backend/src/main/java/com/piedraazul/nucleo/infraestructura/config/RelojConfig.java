package com.piedraazul.nucleo.infraestructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Reloj compartido por los módulos que dependen de "hoy" (ventana de
 * agendamiento, filtrado de franjas ya pasadas).
 *
 * <p>Se inyecta como bean para que los tests puedan fijar la fecha con
 * {@code Clock.fixed(...)} en lugar de depender de la hora real de la máquina.</p>
 */
@Configuration
public class RelojConfig {

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
