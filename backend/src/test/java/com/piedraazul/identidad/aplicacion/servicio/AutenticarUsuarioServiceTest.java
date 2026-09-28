package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.salida.JwtTokenPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.security.JwtProperties;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticarUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private JwtTokenPort jwtTokenPort;
    @Mock
    private JwtProperties jwtProperties;
    @InjectMocks
    private AutenticarUsuarioService servicio;

    @Test
    void entregaUnTokenSiLaClaveCoincide() {
        Usuario usuario = Usuario.reconstituir(3L, "ana", "hash", RolUsuario.MEDICO, 8L, true);
        when(usuarioRepository.buscarPorUsername("ana")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.coincide("demo1234", "hash")).thenReturn(true);
        when(jwtTokenPort.generarToken(usuario)).thenReturn("jwt");
        when(jwtProperties.expirationMs()).thenReturn(86_400_000L);

        var token = servicio.ejecutar("  ANA  ", "demo1234");

        assertEquals("jwt", token.accessToken());
        assertEquals("Bearer", token.tokenType());
        assertEquals(86_400L, token.expiresIn());
        assertEquals(RolUsuario.MEDICO, token.rol());
        assertEquals(8L, token.personaId());
        assertEquals(3L, token.usuarioId());
    }

    @Test
    void rechazaUnUsuarioDesconocido() {
        when(usuarioRepository.buscarPorUsername("ana")).thenReturn(Optional.empty());

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar("ana", "demo1234")
        );
        assertEquals("CREDENCIALES_INVALIDAS", ex.getCodigo());
    }

    @Test
    void rechazaClaveIncorrectaOCuentaInactiva() {
        Usuario inactivo = Usuario.reconstituir(3L, "ana", "hash", RolUsuario.MEDICO, 8L, false);
        when(usuarioRepository.buscarPorUsername("ana")).thenReturn(Optional.of(inactivo));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar("ana", "demo1234")
        );
        assertEquals("CREDENCIALES_INVALIDAS", ex.getCodigo());
    }
}
