package com.piedraazul.nucleo.infraestructura.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Fija el formato de las horas en la API: siempre {@code HH:mm}.
 *
 * <p>Sin esto Jackson alterna entre "08:00" y "08:00:00" según si los segundos
 * son cero, lo que obliga al frontend a normalizar antes de pintar la hora o de
 * cargarla en un {@code <input type="time">}. A la entrada se siguen aceptando
 * ambas formas.</p>
 */
@Configuration
public class JacksonTiempoConfig {

    private static final DateTimeFormatter HORA_SIN_SEGUNDOS = DateTimeFormatter.ofPattern("HH:mm");

    @Bean
    Jackson2ObjectMapperBuilderCustomizer horasConFormatoFijo() {
        return builder -> builder
                .serializers(new LocalTimeSerializer(HORA_SIN_SEGUNDOS))
                .deserializers(new LocalTimeDeserializer(DateTimeFormatter.ISO_LOCAL_TIME));
    }
}
