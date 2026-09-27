package com.piedraazul.identidad.infraestructura.persistencia;

import com.piedraazul.nucleo.dominio.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class UsuarioJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RolUsuario rol;

    @Column(name = "persona_id")
    private Long personaId;

    @Column(nullable = false)
    private boolean activo;

    protected UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(
            Long id,
            String username,
            String passwordHash,
            RolUsuario rol,
            Long personaId,
            boolean activo
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.personaId = personaId;
        this.activo = activo;
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

    public boolean isActivo() {
        return activo;
    }
}
