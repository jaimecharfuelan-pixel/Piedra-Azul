package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.nucleo.dominio.RolUsuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Helper de borde (opción A): lee el JWT del SecurityContext para validaciones
 * de propiedad en controllers. Los *Service de negocio no reciben el rol.
 */
@Component
public class SesionActual {

    public Optional<UsuarioAutenticado> actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    public UsuarioAutenticado exigirUsuario() {
        return actual().orElseThrow(() -> new AccessDeniedException("Debes iniciar sesión"));
    }

    public RolUsuario rol() {
        return exigirUsuario().getRol();
    }

    public Long personaId() {
        return exigirUsuario().getPersonaId();
    }

    public boolean tieneRol(RolUsuario rol) {
        return actual().map(u -> u.getRol() == rol).orElse(false);
    }

    public boolean esAdminOAgendador() {
        return actual().map(u ->
                u.getRol() == RolUsuario.ADMINISTRADOR || u.getRol() == RolUsuario.AGENDADOR
        ).orElse(false);
    }

    /**
     * Paciente (u operador restringido): solo puede operar su propia ficha.
     */
    public void exigirPersonaId(Long esperado) {
        Long propio = personaId();
        if (propio == null || !Objects.equals(propio, esperado)) {
            throw new AccessDeniedException("No puedes operar sobre el recurso de otra persona");
        }
    }

    /**
     * Si el usuario es PACIENTE, fuerza que el id coincida con su personaId.
     * Admin/Agendador/Médico no se restringen aquí.
     */
    public void siEsPacienteExigirPersona(Long pacienteId) {
        if (tieneRol(RolUsuario.PACIENTE)) {
            exigirPersonaId(pacienteId);
        }
    }

    /**
     * Si el usuario es MEDICO, fuerza que el id coincida con su personaId
     * (agenda propia, periodos propios, marcar atendida).
     */
    public void siEsMedicoExigirPersona(Long medicoId) {
        if (tieneRol(RolUsuario.MEDICO)) {
            exigirPersonaId(medicoId);
        }
    }
}
