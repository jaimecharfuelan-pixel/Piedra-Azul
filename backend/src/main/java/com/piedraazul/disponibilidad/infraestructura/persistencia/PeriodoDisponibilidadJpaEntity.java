package com.piedraazul.disponibilidad.infraestructura.persistencia;

import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Horario de atención de un médico vigente en un rango de fechas.
 *
 * <p>Los días de atención se guardan normalizados en la tabla hija
 * {@code periodo_dias_atencion} (una fila por día) en lugar de una lista
 * serializada, para poder consultarlos e indexarlos desde SQL.</p>
 */
@Entity
@Table(
        name = "periodos_disponibilidad",
        indexes = {
                @Index(name = "idx_periodo_medico", columnList = "medico_id"),
                @Index(name = "idx_periodo_medico_vigencia", columnList = "medico_id, fecha_inicio, fecha_fin")
        }
)
public class PeriodoDisponibilidadJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    /** Null = horario vigente hasta que se registre uno nuevo. */
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "periodo_dias_atencion",
            joinColumns = @JoinColumn(
                    name = "periodo_id",
                    nullable = false,
                    foreignKey = @ForeignKey(name = "fk_dias_periodo")
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_periodo_dia",
                    columnNames = {"periodo_id", "dia_semana"}
            ),
            indexes = @Index(name = "idx_dias_periodo", columnList = "periodo_id")
    )
    @Column(name = "dia_semana", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private Set<DiaSemana> diasAtencion = new LinkedHashSet<>();

    @Embedded
    private TimeRange franjaHoraria;

    @Column(name = "duracion_cita_minutos", nullable = false)
    private int duracionCitaMinutos;

    @Column(name = "descanso_entre_citas_minutos", nullable = false)
    private int descansoEntreCitasMinutos;

    protected PeriodoDisponibilidadJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Set<DiaSemana> getDiasAtencion() {
        return diasAtencion;
    }

    public TimeRange getFranjaHoraria() {
        return franjaHoraria;
    }

    public int getDuracionCitaMinutos() {
        return duracionCitaMinutos;
    }

    public int getDescansoEntreCitasMinutos() {
        return descansoEntreCitasMinutos;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMedicoId(Long medicoId) {
        this.medicoId = medicoId;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setDiasAtencion(Set<DiaSemana> diasAtencion) {
        this.diasAtencion.clear();
        if (diasAtencion != null) {
            this.diasAtencion.addAll(diasAtencion);
        }
    }

    public void setFranjaHoraria(TimeRange franjaHoraria) {
        this.franjaHoraria = franjaHoraria;
    }

    public void setDuracionCitaMinutos(int duracionCitaMinutos) {
        this.duracionCitaMinutos = duracionCitaMinutos;
    }

    public void setDescansoEntreCitasMinutos(int descansoEntreCitasMinutos) {
        this.descansoEntreCitasMinutos = descansoEntreCitasMinutos;
    }
}
