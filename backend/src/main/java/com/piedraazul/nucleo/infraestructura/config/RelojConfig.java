package com.piedraazul.nucleo.infraestructura.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj compartido por los módulos que dependen de "hoy" (ventana de
 * agendamiento, filtrado de franjas ya pasadas).
 *
 * <p>Se inyecta como bean para que los tests puedan fijar la fecha con
 * {@code Clock.fixed(...)} en lugar de depender de la hora real de la máquina.</p>
 *
 * <p>La clínica opera en hora de Colombia. El contenedor suele estar en UTC;
 * si "hoy" saliera de esa zona, a partir de las 19:00 un horario que empieza
 * mañana ya se vería vigente.</p>
 */
@Configuration
public class RelojConfig {

    static final ZoneId ZONA_CLINICA = ZoneId.of("America/Bogota");

    @Bean
    Clock clock() {
        return Clock.system(ZONA_CLINICA);
    }
}
