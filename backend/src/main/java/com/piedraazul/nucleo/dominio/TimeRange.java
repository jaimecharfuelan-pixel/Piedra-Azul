package com.piedraazul.nucleo.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Value Object de franja horaria.
 * Protege la regla de negocio: una cita dura mínimo 30 minutos y no se solapa
 * con otra del mismo médico (RF2 / RF3 — franjas e intervalos).
 */
@Embeddable
public class TimeRange {

    public static final long DURACION_MINIMA_MINUTOS = 30L;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    protected TimeRange() {
        // JPA
    }

    public TimeRange(LocalTime horaInicio, LocalTime horaFin) {
        Objects.requireNonNull(horaInicio, "horaInicio es obligatorio");
        Objects.requireNonNull(horaFin, "horaFin es obligatorio");
        if (!horaFin.isAfter(horaInicio)) {
            throw ReglaDeNegocioException.de(
                    "RANGO_HORARIO_INVALIDO",
                    "La hora de fin debe ser posterior a la hora de inicio"
            );
        }
        long minutos = Duration.between(horaInicio, horaFin).toMinutes();
        if (minutos < DURACION_MINIMA_MINUTOS) {
            throw ReglaDeNegocioException.de(
                    "DURACION_INVALIDA",
                    "La duración mínima de una franja/cita es de " + DURACION_MINIMA_MINUTOS + " minutos"
            );
        }
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    /**
     * Indica si este rango se solapa con otro (misma lógica que citas del mismo médico).
     */
    public boolean solapaCon(TimeRange otro) {
        Objects.requireNonNull(otro, "otro es obligatorio");
        return this.horaInicio.isBefore(otro.horaFin) && otro.horaInicio.isBefore(this.horaFin);
    }

    public long duracionMinutos() {
        return Duration.between(horaInicio, horaFin).toMinutes();
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimeRange that)) {
            return false;
        }
        return Objects.equals(horaInicio, that.horaInicio) && Objects.equals(horaFin, that.horaFin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(horaInicio, horaFin);
    }

    @Override
    public String toString() {
        return "TimeRange{" + horaInicio + " - " + horaFin + '}';
    }
}
