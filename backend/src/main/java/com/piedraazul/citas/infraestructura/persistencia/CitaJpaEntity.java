package com.piedraazul.citas.infraestructura.persistencia;

import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Cita persistida. Los índices siguen los dos accesos reales: agenda del médico
 * por fecha (RF1 y cálculo de franjas) y "mis citas" del paciente.
 */
@Entity
@Table(
        name = "citas",
        indexes = {
                @Index(name = "idx_cita_medico_fecha", columnList = "medico_id, fecha"),
                @Index(name = "idx_cita_medico_fecha_estado", columnList = "medico_id, fecha, estado"),
                @Index(name = "idx_cita_paciente", columnList = "paciente_id")
        }
)
public class CitaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /** Aporta las columnas hora_inicio y hora_fin. */
    @Embedded
    private TimeRange rango;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCita estado;

    protected CitaJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public TimeRange getRango() {
        return rango;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setRango(TimeRange rango) {
        this.rango = rango;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }
}
