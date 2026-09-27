package com.piedraazul.identidad.infraestructura.security;

import com.piedraazul.nucleo.dominio.RolUsuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal JWT: username + rol + personaId para las validaciones de propiedad en controllers.
 */
public class UsuarioAutenticado implements UserDetails {

    private final Long usuarioId;
    private final String username;
    private final RolUsuario rol;
    private final Long personaId;
    private final boolean activo;

    public UsuarioAutenticado(Long usuarioId, String username, RolUsuario rol, Long personaId, boolean activo) {
        this.usuarioId = usuarioId;
        this.username = username;
        this.rol = rol;
        this.personaId = personaId;
        this.activo = activo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Long getPersonaId() {
        return personaId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return activo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }
}
