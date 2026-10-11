package com.piedraazul.identidad.aplicacion.servicio;

import com.piedraazul.identidad.aplicacion.puertos.entrada.GestionarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.ProveedorIdentidadPort;
import com.piedraazul.identidad.aplicacion.puertos.salida.UsuarioRepository;
import com.piedraazul.identidad.dominio.Usuario;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.RegistrarPersonaPort;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Alta de usuarios (admin) y registro público de paciente (mismo {@code crear}).
 * Persiste el vínculo local (rol + personaId) y sincroniza credenciales en Keycloak.
 */
@Service
@Transactional
public class GestionarUsuarioService implements GestionarUsuarioUseCase {

    private static final Logger log = LoggerFactory.getLogger(GestionarUsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final RegistrarPersonaPort registrarPersonaPort;
    private final ProveedorIdentidadPort proveedorIdentidad;

    public GestionarUsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoderPort passwordEncoder,
            RegistrarPersonaPort registrarPersonaPort,
            ProveedorIdentidadPort proveedorIdentidad
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrarPersonaPort = registrarPersonaPort;
        this.proveedorIdentidad = proveedorIdentidad;
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

        sincronizarConProveedor(usuario.getUsername(), comando.password(), usuario.getRol());
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
        Usuario guardado = usuarioRepository.guardar(usuario);
        try {
            proveedorIdentidad.sincronizarEstado(guardado.getUsername(), activo);
        } catch (RuntimeException ex) {
            log.warn("No se sincronizó el estado de {} en el IdP: {}", guardado.getUsername(), ex.getMessage());
        }
        return toDto(guardado);
    }

    /**
     * La ficha local no debe perderse si Keycloak aún no responde (p. ej. seed al arrancar).
     * Los usuarios demo ya vienen en el realm import; el alta por API reintenta vía Admin.
     */
    private void sincronizarConProveedor(String username, String password, RolUsuario rol) {
        try {
            proveedorIdentidad.asegurarUsuario(username, password, rol);
        } catch (RuntimeException ex) {
            log.warn("Usuario {} quedó local; Keycloak no sincronizó aún: {}", username, ex.getMessage());
        }
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
            if (datos.cedula() == null || datos.cedula().isBlank()) {
                throw ReglaDeNegocioException.de(
                        "DATOS_PERSONA_OBLIGATORIOS",
                        "Para crear un paciente debes indicar la cédula"
                );
            }
        }
    }

    private DatosPersonaDTO datosConUsuarioId(Long usuarioId, DatosPersonaDTO datos) {
        if (datos == null) {
            return new DatosPersonaDTO(usuarioId, null, null, null, null);
        }
        return new DatosPersonaDTO(
                usuarioId,
                datos.nombreCompleto(),
                datos.telefono(),
                datos.especialidadId(),
                datos.cedula()
        );
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
