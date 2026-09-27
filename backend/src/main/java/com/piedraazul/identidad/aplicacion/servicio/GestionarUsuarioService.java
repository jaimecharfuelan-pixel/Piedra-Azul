package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.entrada.GestionarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.RegistrarPersonaPort;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alta de usuarios (admin) y registro público de paciente (mismo {@code crear}).
 */
@Service
@Transactional
public class GestionarUsuarioService implements GestionarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final RegistrarPersonaPort registrarPersonaPort;

    public GestionarUsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            RegistrarPersonaPort registrarPersonaPort
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrarPersonaPort = registrarPersonaPort;
    }

    @Override
    public UsuarioResponseDTO crear(CrearUsuarioCommand comando) {
        validarUsernameDisponible(comando.username());
        validarDatosPersonaSegunRol(comando.rol(), comando.datosPersona());

        String hash = passwordEncoder.codificar(comando.password());
        Usuario usuario = usuarioRepository.guardar(
                new Usuario(comando.username(), hash, comando.rol(), null)
        );

        Long personaId = registrarPersonaPort.crearPersonaParaUsuario(
                comando.rol(),
                datosConUsuarioId(usuario.getId(), comando.datosPersona())
        );
        if (personaId != null) {
            usuario.vincularPersona(personaId);
            usuario = usuarioRepository.guardar(usuario);
        }

        return toDto(usuario);
    }

    @Override
    public UsuarioResponseDTO cambiarEstado(Long id, boolean activo) {
        Usuario usuario = requerir(id);
        if (activo) {
            usuario.activar();
        } else {
            usuario.desactivar();
        }
        return toDto(usuarioRepository.guardar(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return toDto(requerir(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.listarTodos().stream().map(this::toDto).toList();
    }

    private void validarUsernameDisponible(String username) {
        if (usuarioRepository.existePorUsername(username.trim().toLowerCase())) {
            throw ReglaDeNegocioException.de(
                    "USERNAME_YA_REGISTRADO",
                    "Ya existe un usuario con ese nombre de acceso"
            );
        }
    }

    private void validarDatosPersonaSegunRol(RolUsuario rol, DatosPersonaDTO datos) {
        if (rol == RolUsuario.MEDICO) {
            if (datos == null || datos.nombreCompleto() == null || datos.nombreCompleto().isBlank()) {
                throw ReglaDeNegocioException.de(
                        "DATOS_PERSONA_OBLIGATORIOS",
                        "Para crear un médico debes indicar el nombre completo"
                );
            }
            if (datos.especialidadId() == null) {
                throw ReglaDeNegocioException.de(
                        "DATOS_PERSONA_OBLIGATORIOS",
                        "Para crear un médico debes indicar la especialidad"
                );
            }
        }
        if (rol == RolUsuario.PACIENTE) {
            if (datos == null || datos.nombreCompleto() == null || datos.nombreCompleto().isBlank()) {
                throw ReglaDeNegocioException.de(
                        "DATOS_PERSONA_OBLIGATORIOS",
                        "Para crear un paciente debes indicar el nombre completo"
                );
            }
        }
    }

    private DatosPersonaDTO datosConUsuarioId(Long usuarioId, DatosPersonaDTO datos) {
        if (datos == null) {
            return new DatosPersonaDTO(usuarioId, null, null, null);
        }
        return new DatosPersonaDTO(usuarioId, datos.nombreCompleto(), datos.telefono(), datos.especialidadId());
    }

    private Usuario requerir(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> RecursoNoEncontradoException.de(
                        "USUARIO_NO_ENCONTRADO",
                        "No existe usuario con id " + id
                ));
    }

    private UsuarioResponseDTO toDto(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getPersonaId(),
                usuario.estaActivo()
        );
    }
}
