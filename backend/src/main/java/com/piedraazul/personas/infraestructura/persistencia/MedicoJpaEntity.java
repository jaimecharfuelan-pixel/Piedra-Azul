package com.piedraazul.personas.infraestructura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class MedicoJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 180)
    private String nombreCompleto;

    @Column(name = "especialidad_id", nullable = false)
    private Long especialidadId;

    @Column(nullable = false)
    private boolean activo;

    protected MedicoJpaEntity() {
    }

    public MedicoJpaEntity(Long id, String nombreCompleto, Long especialidadId, boolean activo) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidadId = especialidadId;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public void setEspecialidadId(Long especialidadId) {
        this.especialidadId = especialidadId;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
