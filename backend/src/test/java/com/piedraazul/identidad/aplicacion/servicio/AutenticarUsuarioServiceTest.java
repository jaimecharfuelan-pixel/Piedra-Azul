package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.salida.ProveedorIdentidadPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
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
    private ProveedorIdentidadPort proveedorIdentidad;
    @InjectMocks
    private AutenticarUsuarioService servicio;

    @Test
    void entregaUnTokenSiElProveedorAutentica() {
        Usuario usuario = Usuario.reconstituir(3L, "ana", "hash", RolUsuario.MEDICO, 8L, true);
        when(usuarioRepository.buscarPorUsername("ana")).thenReturn(Optional.of(usuario));
        when(proveedorIdentidad.autenticar("ana", "demo1234"))
                .thenReturn(new ProveedorIdentidadPort.TokenProveedor("jwt-keycloak", "Bearer", 86_400L));

        var token = servicio.ejecutar("  ANA  ", "demo1234");

        assertEquals("jwt-keycloak", token.accessToken());
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
    void rechazaCuentaInactiva() {
        Usuario inactivo = Usuario.reconstituir(3L, "ana", "hash", RolUsuario.MEDICO, 8L, false);
        when(usuarioRepository.buscarPorUsername("ana")).thenReturn(Optional.of(inactivo));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.ejecutar("ana", "demo1234")
        );
        assertEquals("CREDENCIALES_INVALIDAS", ex.getCodigo());
    }
}
