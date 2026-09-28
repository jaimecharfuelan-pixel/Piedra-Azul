package com.piedraazul.identidad.dominio;

import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void normalizaElUsernameYNaceActivoSinPersona() {
        Usuario usuario = new Usuario("  Ana.Medico  ", "hash", RolUsuario.MEDICO, null);

        assertEquals("ana.medico", usuario.getUsername());
        assertEquals("hash", usuario.getPasswordHash());
        assertEquals(RolUsuario.MEDICO, usuario.getRol());
        assertNull(usuario.getPersonaId());
        assertTrue(usuario.estaActivo());
    }

    @Test
    void rechazaUsernameVacio() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new Usuario("  ", "hash", RolUsuario.PACIENTE, 1L)
        );
        assertEquals("USERNAME_INVALIDO", ex.getCodigo());
    }

    @Test
    void exigeHashYRol() {
        assertThrows(NullPointerException.class, () -> new Usuario("ana", null, RolUsuario.PACIENTE, 1L));
        assertThrows(NullPointerException.class, () -> new Usuario("ana", "hash", null, 1L));
    }

    @Test
    void laClaveSeCompruebaPorElPuerto() {
        Usuario usuario = new Usuario("ana", "hash-guardado", RolUsuario.PACIENTE, 4L);
        PasswordEncoderPort encoder = new PasswordEncoderPort() {
            @Override
            public String codificar(String passwordPlano) {
                return "hash-" + passwordPlano;
            }

            @Override
            public boolean coincide(String passwordPlano, String hash) {
                return hash.equals("hash-" + passwordPlano);
            }
        };

        assertTrue(usuario.coincideCon("guardado", encoder));
        assertFalse(usuario.coincideCon("otra", encoder));
    }

    @Test
    void sePuedeVincularLaPersonaYCambiarElEstado() {
        Usuario usuario = Usuario.reconstituir(1L, "ana", "hash", RolUsuario.MEDICO, null, true);

        usuario.vincularPersona(20L);
        usuario.desactivar();

        assertEquals(20L, usuario.getPersonaId());
        assertFalse(usuario.estaActivo());
        usuario.activar();
        assertTrue(usuario.estaActivo());
    }
}
