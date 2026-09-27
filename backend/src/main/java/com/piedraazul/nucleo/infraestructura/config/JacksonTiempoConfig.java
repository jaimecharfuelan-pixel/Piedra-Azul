package com.piedraazul.nucleo.infraestructura.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Serializa {@link java.time.LocalTime} como {@code HH:mm} en la API JSON.
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
