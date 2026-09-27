package com.piedraazul.identidad.dominio;

import com.piedraazul.identidad.aplicacion.puertos.salida.PasswordEncoderPort;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Objects;

/**
 * Cuenta de acceso al sistema (módulo Identidad). El rol vive aquí, no en Medico/Paciente.
 */
public class Usuario {

    private Long id;
    private String username;
    private String passwordHash;
    private RolUsuario rol;
    private Long personaId;
    private boolean activo;

    public Usuario(String username, String passwordHash, RolUsuario rol, Long personaId) {
        this.username = validarUsername(username);
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
        this.personaId = personaId;
        this.activo = true;
    }

    private Usuario(Long id, String username, String passwordHash, RolUsuario rol, Long personaId, boolean activo) {
        this.id = id;
        this.username = validarUsername(username);
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash es obligatorio");
        this.rol = Objects.requireNonNull(rol, "rol es obligatorio");
        this.personaId = personaId;
        this.activo = activo;
    }

    public static Usuario reconstituir(
            Long id,
            String username,
            String passwordHash,
            RolUsuario rol,
            Long personaId,
            boolean activo
    ) {
        return new Usuario(id, username, passwordHash, rol, personaId, activo);
    }

    public boolean coincideCon(String passwordPlano, PasswordEncoderPort encoder) {
        return encoder.coincide(passwordPlano, passwordHash);
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    /** Enlaza la ficha Medico/Paciente creada tras persistir el usuario. */
    public void vincularPersona(Long personaId) {
        this.personaId = personaId;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public Long getPersonaId() {
        return personaId;
    }

    public boolean estaActivo() {
        return activo;
    }

    void asignarId(Long id) {
        this.id = id;
    }

    private static String validarUsername(String username) {
        if (username == null || username.isBlank()) {
            throw ReglaDeNegocioException.de("USERNAME_INVALIDO", "El nombre de usuario es obligatorio");
        }
        return username.trim().toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Usuario that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
