package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.ProveedorIdentidadPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Emisor local con claims compatibles con Keycloak ({@code realm_access.roles}).
 * Solo activo con {@code app.security.provider=local} (tests / dev sin contenedor).
 */
@Component
@ConditionalOnProperty(name = "app.security.provider", havingValue = "local", matchIfMissing = true)
public class LocalJwtProveedorIdentidadAdapter implements ProveedorIdentidadPort {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtLocalProperties properties;

    public LocalJwtProveedorIdentidadAdapter(
            UsuarioRepository usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            JwtEncoder jwtEncoder,
            JwtLocalProperties properties
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @Override
    public TokenProveedor autenticar(String username, String passwordPlano) {
        String normalizado = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.buscarPorUsername(normalizado)
                .orElseThrow(this::credencialesInvalidas);
        if (!usuario.estaActivo() || !usuario.coincideCon(passwordPlano, passwordEncoder)) {
            throw credencialesInvalidas();
        }

        Instant now = Instant.now();
        Instant exp = now.plusMillis(properties.expirationMs());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(exp)
                .subject(usuario.getUsername())
                .claim("preferred_username", usuario.getUsername())
                .claim("realm_access", Map.of("roles", List.of(usuario.getRol().name())))
                .claim("rol", usuario.getRol().name())
                .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims
        )).getTokenValue();

        return new TokenProveedor(token, "Bearer", properties.expirationMs() / 1000);
    }

    @Override
    public void asegurarUsuario(String username, String passwordPlano, RolUsuario rol) {
        // En modo local el usuario ya vive en la BD de la app.
    }

    @Override
    public void sincronizarEstado(String username, boolean activo) {
        // En modo local el estado se gestiona solo en la BD.
    }

    private ReglaDeNegocioException credencialesInvalidas() {
        return ReglaDeNegocioException.de(
                "CREDENCIALES_INVALIDAS",
                "Usuario o contraseña incorrectos"
        );
    }
}
