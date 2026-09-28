package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.RegistrarPersonaPort;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionarUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;
    @Mock
    private RegistrarPersonaPort registrarPersonaPort;
    @InjectMocks
    private GestionarUsuarioService servicio;

    @Test
    void creaUnPacienteYLeVinculaLaFicha() {
        when(usuarioRepository.existePorUsername("juan")).thenReturn(false);
        when(passwordEncoder.codificar("demo1234")).thenReturn("hash");
        when(registrarPersonaPort.crearPersonaParaUsuario(any(), any())).thenReturn(20L);
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            Long id = usuario.getId() == null ? 3L : usuario.getId();
            return Usuario.reconstituir(
                    id, usuario.getUsername(), usuario.getPasswordHash(),
                    usuario.getRol(), usuario.getPersonaId(), usuario.estaActivo()
            );
        });

        var dto = servicio.crear(new CrearUsuarioCommand(
                " Juan ", "demo1234", RolUsuario.PACIENTE, new DatosPersonaDTO(null, "Juan Ramírez", "300", null)
        ));

        assertEquals(3L, dto.id());
        assertEquals("juan", dto.username());
        assertEquals(20L, dto.personaId());
        assertEquals(RolUsuario.PACIENTE, dto.rol());
    }

    @Test
    void unAdministradorNoRecibeFichaDePersona() {
        when(usuarioRepository.existePorUsername("admin")).thenReturn(false);
        when(passwordEncoder.codificar("demo1234")).thenReturn("hash");
        when(registrarPersonaPort.crearPersonaParaUsuario(any(), any())).thenReturn(null);
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> {
            Usuario usuario = inv.getArgument(0);
            return Usuario.reconstituir(1L, usuario.getUsername(), usuario.getPasswordHash(), usuario.getRol(), null, true);
        });

        var dto = servicio.crear(new CrearUsuarioCommand("admin", "demo1234", RolUsuario.ADMINISTRADOR, null));

        assertNull(dto.personaId());
    }

    @Test
    void rechazaUnUsernameRepetido() {
        when(usuarioRepository.existePorUsername("juan")).thenReturn(true);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.crear(new CrearUsuarioCommand(
                        "juan", "demo1234", RolUsuario.PACIENTE, new DatosPersonaDTO(null, "Juan", "300", null)
                ))
        );
        assertEquals("USERNAME_YA_REGISTRADO", ex.getCodigo());
    }

    @Test
    void unMedicoExigeEspecialidad() {
        when(usuarioRepository.existePorUsername("ana")).thenReturn(false);

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.crear(new CrearUsuarioCommand(
                        "ana", "demo1234", RolUsuario.MEDICO, new DatosPersonaDTO(null, "Ana", null, null)
                ))
        );
        assertEquals("DATOS_PERSONA_OBLIGATORIOS", ex.getCodigo());
    }

    @Test
    void buscarPorIdFallaSiNoExiste() {
        when(usuarioRepository.buscarPorId(9L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.buscarPorId(9L)
        );
        assertEquals("USUARIO_NO_ENCONTRADO", ex.getCodigo());
    }
}
