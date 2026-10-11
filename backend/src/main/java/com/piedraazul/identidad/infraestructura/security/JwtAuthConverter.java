package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.nucleo.dominio.RolUsuario;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Convierte el JWT (Keycloak o emisor local) en {@link UsuarioAutenticado}
 * enriquecido con {@code personaId} desde la tabla local de usuarios.
 * La matriz de roles en {@link SecurityConfig} no cambia.
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UsuarioRepository usuarioRepository;

    public JwtAuthConverter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String username = extraerUsername(jwt);
        Usuario usuario = usuarioRepository.buscarPorUsername(username)
                .filter(Usuario::estaActivo)
                .orElseThrow(() -> new BadCredentialsException(
                        "Usuario local no encontrado o inactivo: " + username
                ));

        RolUsuario rol = extraerRol(jwt).orElse(usuario.getRol());
        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuario.getId(),
                usuario.getUsername(),
                rol,
                usuario.getPersonaId(),
                usuario.estaActivo()
        );
        return new UsernamePasswordAuthenticationToken(
                principal,
                jwt.getTokenValue(),
                principal.getAuthorities()
        );
    }

    private static String extraerUsername(Jwt jwt) {
        String preferred = jwt.getClaimAsString("preferred_username");
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim().toLowerCase(Locale.ROOT);
        }
        String subject = jwt.getSubject();
        return subject == null ? "" : subject.trim().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private static Optional<RolUsuario> extraerRol(Jwt jwt) {
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof Map<?, ?> map) {
            Object roles = map.get("roles");
            if (roles instanceof Collection<?> collection) {
                for (Object role : collection) {
                    Optional<RolUsuario> parsed = parseRol(String.valueOf(role));
                    if (parsed.isPresent()) {
                        return parsed;
                    }
                }
            }
        }
        Object claimRol = jwt.getClaim("rol");
        if (claimRol != null) {
            return parseRol(String.valueOf(claimRol));
        }
        return Optional.empty();
    }

    private static Optional<RolUsuario> parseRol(String raw) {
        String nombre = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
        if (nombre.startsWith("ROLE_")) {
            nombre = nombre.substring(5);
        }
        try {
            return Optional.of(RolUsuario.valueOf(nombre));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
