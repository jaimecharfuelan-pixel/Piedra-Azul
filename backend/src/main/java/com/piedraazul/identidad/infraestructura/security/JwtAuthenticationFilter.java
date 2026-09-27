package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.identidad.aplicacion.puertos.salida.JwtTokenPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenPort jwtTokenPort;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtTokenPort jwtTokenPort, UsuarioRepository usuarioRepository) {
        this.jwtTokenPort = jwtTokenPort;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            String username = jwtTokenPort.extraerUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                usuarioRepository.buscarPorUsername(username).ifPresent(usuario -> {
                    if (jwtTokenPort.esTokenValido(token, usuario)) {
                        establecerContexto(request, usuario);
                    }
                });
            }
        } catch (RuntimeException ignored) {
            // Token inválido: se deja sin autenticación; SecurityConfig responderá 401/403.
        }

        filterChain.doFilter(request, response);
    }

    private void establecerContexto(HttpServletRequest request, Usuario usuario) {
        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getPersonaId(),
                usuario.estaActivo()
        );
        var authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
