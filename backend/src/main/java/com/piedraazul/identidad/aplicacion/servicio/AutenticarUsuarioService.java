package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.entrada.AutenticarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.salida.JwtTokenPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.dto.TokenResponseDTO;
import com.piedraazul.identidad.infraestructura.security.JwtProperties;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenPort jwtTokenPort;
    private final JwtProperties jwtProperties;

    public AutenticarUsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            JwtTokenPort jwtTokenPort,
            JwtProperties jwtProperties
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenPort = jwtTokenPort;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public TokenResponseDTO ejecutar(String username, String passwordPlano) {
        String normalizado = username == null ? "" : username.trim().toLowerCase();
        Usuario usuario = usuarioRepository.buscarPorUsername(normalizado)
                .orElseThrow(this::credencialesInvalidas);

        if (!usuario.estaActivo() || !usuario.coincideCon(passwordPlano, passwordEncoder)) {
            throw credencialesInvalidas();
        }

        String token = jwtTokenPort.generarToken(usuario);
        return new TokenResponseDTO(
                token,
                "Bearer",
                jwtProperties.expirationMs() / 1000,
                usuario.getRol(),
                usuario.getPersonaId(),
                usuario.getId()
        );
    }

    private ReglaDeNegocioException credencialesInvalidas() {
        return ReglaDeNegocioException.de(
                "CREDENCIALES_INVALIDAS",
                "Usuario o contraseña incorrectos"
        );
    }
}
