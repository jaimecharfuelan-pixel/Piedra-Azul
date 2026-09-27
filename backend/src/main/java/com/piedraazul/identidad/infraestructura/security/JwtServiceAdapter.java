package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.identidad.aplicacion.puertos.salida.JwtTokenPort;
import com.piedraazul.identidad.dominio.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtServiceAdapter implements JwtTokenPort {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtServiceAdapter(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    @Override
    public String generarToken(Usuario usuario) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.expirationMs());
        return Jwts.builder()
                .claims(Map.of(
                        "rol", usuario.getRol().name(),
                        "personaId", usuario.getPersonaId() == null ? -1L : usuario.getPersonaId(),
                        "usuarioId", usuario.getId()
                ))
                .subject(usuario.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Override
    public String extraerUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public boolean esTokenValido(String token, Usuario usuario) {
        try {
            Claims claims = parseClaims(token);
            return usuario.getUsername().equals(claims.getSubject())
                    && claims.getExpiration().after(new Date())
                    && usuario.estaActivo();
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long expirationMs() {
        return properties.expirationMs();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
