package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de historial que queda cuando una cita se marca como atendida.
 *
 * <p>La Cita no se borra al atenderse: queda con estado ATENDIDA y además se
 * crea esta Consulta, que es lo que se muestra en el historial del paciente y
 * del médico.</p>
 */
public class Consulta {

    public static final int MAXIMO_OBSERVACIONES = 2000;

    private Long id;
    private final Long citaId;
    private final Long medicoId;
    private final Long pacienteId;
    private final LocalDate fecha;
    private final String observaciones;
    private final LocalDateTime fechaRegistro;

    private Consulta(
            Long id,
            Long citaId,
            Long medicoId,
            Long pacienteId,
            LocalDate fecha,
            String observaciones,
            LocalDateTime fechaRegistro
    ) {
        this.id = id;
        this.citaId = Objects.requireNonNull(citaId, "citaId es obligatorio");
        this.medicoId = Objects.requireNonNull(medicoId, "medicoId es obligatorio");
        this.pacienteId = Objects.requireNonNull(pacienteId, "pacienteId es obligatorio");
        this.fecha = Objects.requireNonNull(fecha, "fecha es obligatoria");
        this.observaciones = validarObservaciones(observaciones);
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro, "fechaRegistro es obligatoria");
    }

    /**
     * Sólo se construye desde una cita ya atendida: es la garantía de que el
     * historial no tenga consultas de citas canceladas o todavía pendientes.
     */
    public static Consulta desdeCita(Cita cita, String observaciones) {
        Objects.requireNonNull(cita, "cita es obligatoria");
        if (cita.getEstado() != EstadoCita.ATENDIDA) {
            throw ReglaDeNegocioException.de(
                    "CONSULTA_SIN_CITA_ATENDIDA",
                    "Solo se registra consulta de una cita atendida"
            );
        }
        return new Consulta(
                null,
                cita.getId(),
                cita.getMedicoId(),
                cita.getPacienteId(),
                cita.getFecha(),
                observaciones,
                LocalDateTime.now()
        );
    }

    public static Consulta reconstituir(
            Long id,
            Long citaId,
            Long medicoId,
            Long pacienteId,
            LocalDate fecha,
            String observaciones,
            LocalDateTime fechaRegistro
    ) {
        return new Consulta(id, citaId, medicoId, pacienteId, fecha, observaciones, fechaRegistro);
    }

    public Long getId() {
        return id;
    }

    public Long getCitaId() {
        return citaId;
    }

    public Long getMedicoId() {
        return medicoId;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    private static String validarObservaciones(String observaciones) {
        if (observaciones == null) {
            return "";
        }
        String limpio = observaciones.trim();
        if (limpio.length() > MAXIMO_OBSERVACIONES) {
            throw ReglaDeNegocioException.de(
                    "OBSERVACIONES_DEMASIADO_LARGAS",
                    "Las observaciones no pueden pasar de " + MAXIMO_OBSERVACIONES + " caracteres"
            );
        }
        return limpio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Consulta that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
