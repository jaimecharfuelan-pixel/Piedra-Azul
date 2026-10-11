package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.entrada.AutenticarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.salida.ProveedorIdentidadPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.dto.TokenResponseDTO;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Login: delega la emisión del JWT al proveedor (Keycloak) y enriquece con
 * {@code personaId} / rol desde el usuario local (dominio de autorización intacto).
 */
@Service
@Transactional(readOnly = true)
public class AutenticarUsuarioService implements AutenticarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final ProveedorIdentidadPort proveedorIdentidad;

    public AutenticarUsuarioService(
            UsuarioRepository usuarioRepository,
            ProveedorIdentidadPort proveedorIdentidad
    ) {
        this.usuarioRepository = usuarioRepository;
        this.proveedorIdentidad = proveedorIdentidad;
    }

    @Override
    public TokenResponseDTO ejecutar(String username, String passwordPlano) {
        String normalizado = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.buscarPorUsername(normalizado)
                .orElseThrow(this::credencialesInvalidas);

        if (!usuario.estaActivo()) {
            throw credencialesInvalidas();
        }

        ProveedorIdentidadPort.TokenProveedor token =
                proveedorIdentidad.autenticar(normalizado, passwordPlano);

        return new TokenResponseDTO(
                token.accessToken(),
                token.tokenType() == null || token.tokenType().isBlank() ? "Bearer" : token.tokenType(),
                token.expiresInSeconds(),
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
