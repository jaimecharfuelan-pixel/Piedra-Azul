package com.piedraazul.identidad.infraestructura.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties({KeycloakProperties.class, JwtLocalProperties.class})
public class SecurityBeansConfig {

    @Bean
    @ConditionalOnProperty(name = "app.security.provider", havingValue = "local", matchIfMissing = true)
    SecretKey localJwtSecretKey(JwtLocalProperties properties) {
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException ex) {
            bytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.provider", havingValue = "local", matchIfMissing = true)
    JwtEncoder localJwtEncoder(SecretKey localJwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(localJwtSecretKey));
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.provider", havingValue = "local", matchIfMissing = true)
    JwtDecoder localJwtDecoder(SecretKey localJwtSecretKey, JwtLocalProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(localJwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.provider", havingValue = "keycloak")
    JwtDecoder keycloakJwtDecoder(KeycloakProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwkSetUri()).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuerUri()));
        return decoder;
    }
}
