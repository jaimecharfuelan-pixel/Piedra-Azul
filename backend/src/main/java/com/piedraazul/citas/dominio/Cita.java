package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Cita médica reservada por un paciente con un médico (RF1, RF2).
 *
 * <p>Ciclo de vida: nace PROGRAMADA y desde ahí puede pasar a CANCELADA o a
 * ATENDIDA. Ninguno de esos dos estados finales se puede volver a modificar.</p>
 */
public class Cita {

    private Long id;
    private final Long pacienteId;
    private final Long medicoId;
    private LocalDate fecha;
    private TimeRange rango;
    private EstadoCita estado;

    private Cita(Long id, Long pacienteId, Long medicoId, LocalDate fecha, TimeRange rango, EstadoCita estado) {
        this.id = id;
        this.pacienteId = requerir(pacienteId, "CITA_PACIENTE_OBLIGATORIO", "La cita debe tener un paciente");
        this.medicoId = requerir(medicoId, "CITA_MEDICO_OBLIGATORIO", "La cita debe tener un médico");
        this.fecha = requerir(fecha, "CITA_FECHA_OBLIGATORIA", "La fecha de la cita es obligatoria");
        this.rango = requerir(rango, "CITA_RANGO_OBLIGATORIO", "La franja horaria de la cita es obligatoria");
        this.estado = requerir(estado, "CITA_ESTADO_OBLIGATORIO", "El estado de la cita es obligatorio");
    }

    public static Cita crear(Long pacienteId, Long medicoId, LocalDate fecha, TimeRange rango) {
        return new Cita(null, pacienteId, medicoId, fecha, rango, EstadoCita.PROGRAMADA);
    }

    public static Cita reconstituir(
            Long id,
            Long pacienteId,
            Long medicoId,
            LocalDate fecha,
            TimeRange rango,
            EstadoCita estado
    ) {
        return new Cita(id, pacienteId, medicoId, fecha, rango, estado);
    }

    public void cancelar() {
        exigirProgramada("cancelar");
        this.estado = EstadoCita.CANCELADA;
    }

    public void reagendar(LocalDate nuevaFecha, TimeRange nuevoRango) {
        exigirProgramada("reagendar");
        this.fecha = requerir(nuevaFecha, "CITA_FECHA_OBLIGATORIA", "La nueva fecha de la cita es obligatoria");
        this.rango = requerir(nuevoRango, "CITA_RANGO_OBLIGATORIO", "La nueva franja horaria es obligatoria");
    }

    /**
     * La cita se cumplió. No se borra: queda ATENDIDA para trazabilidad y el
     * servicio crea además la Consulta que alimenta el historial.
     */
    public void marcarComoAtendida() {
        exigirProgramada("marcar como atendida");
        this.estado = EstadoCita.ATENDIDA;
    }

    public boolean estaProgramada() {
        return estado == EstadoCita.PROGRAMADA;
    }

    public Long getId() {
        return id;
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

    public TimeRange getRango() {
        return rango;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    private void exigirProgramada(String accion) {
        if (!estaProgramada()) {
            throw ReglaDeNegocioException.de(
                    "CITA_NO_MODIFICABLE",
                    "No se puede " + accion + " una cita que ya está " + estado
            );
        }
    }

    private static <T> T requerir(T valor, String codigo, String mensaje) {
        if (valor == null) {
            throw ReglaDeNegocioException.de(codigo, mensaje);
        }
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cita that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
