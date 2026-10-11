package com.piedraazul.identidad.infraestructura.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Emisor JWT local (Nimbus) solo cuando {@code app.security.provider=local}.
 * No usa JJWT; sirve para tests y desarrollo sin Keycloak.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtLocalProperties(String secret, long expirationMs, String issuer) {
}
