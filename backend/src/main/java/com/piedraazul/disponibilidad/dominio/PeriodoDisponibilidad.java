package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.PeriodoDisponibilidadRef;
import com.piedraazul.nucleo.dominio.DiaSemana;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Contrato de horario de un médico válido sólo dentro de un rango de fechas (RF3).
 *
 * <p>Un médico puede tener VARIOS periodos a lo largo del tiempo (ej. lunes a
 * viernes las primeras dos semanas y luego martes y jueves). {@code fechaFin == null}
 * significa "vigente hasta que se cree un periodo nuevo": al registrar un periodo que
 * empieza mientras otro sigue abierto, el servicio cierra el anterior con
 * {@link #cerrarEn(LocalDate)} para que nunca se solapen dos periodos del mismo médico.</p>
 */
public class PeriodoDisponibilidad implements PeriodoDisponibilidadRef {

    /** Una cita nunca puede durar menos que esto (misma regla que {@link TimeRange}). */
    public static final int DURACION_CITA_MINIMA_MINUTOS = 30;
    public static final int DESCANSO_MAXIMO_MINUTOS = 120;

    private Long id;
    private final Long medicoId;
    private final LocalDate fechaInicio;
    private LocalDate fechaFin;
    private final Set<DiaSemana> diasAtencion;
    private final TimeRange franjaHoraria;
    private final int duracionCitaMinutos;
    private final int descansoEntreCitasMinutos;

    public PeriodoDisponibilidad(
            Long medicoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Set<DiaSemana> diasAtencion,
            TimeRange franjaHoraria,
            int duracionCitaMinutos,
            int descansoEntreCitasMinutos
    ) {
        this.medicoId = requerir(medicoId, "PERIODO_MEDICO_OBLIGATORIO", "El periodo debe estar asociado a un médico");
        this.fechaInicio = requerir(fechaInicio, "PERIODO_FECHA_INICIO_OBLIGATORIA", "La fecha de inicio del periodo es obligatoria");
        this.franjaHoraria = requerir(franjaHoraria, "PERIODO_SIN_FRANJA_HORARIA", "La franja horaria de atención es obligatoria");
        this.diasAtencion = validarDias(diasAtencion);
        this.duracionCitaMinutos = validarDuracion(duracionCitaMinutos, franjaHoraria);
        this.descansoEntreCitasMinutos = validarDescanso(descansoEntreCitasMinutos);
        this.fechaFin = validarFechaFin(fechaInicio, fechaFin);
    }

    private PeriodoDisponibilidad(
            Long id,
            Long medicoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Set<DiaSemana> diasAtencion,
            TimeRange franjaHoraria,
            int duracionCitaMinutos,
            int descansoEntreCitasMinutos
    ) {
        this(medicoId, fechaInicio, fechaFin, diasAtencion, franjaHoraria, duracionCitaMinutos, descansoEntreCitasMinutos);
        this.id = id;
    }

    public static PeriodoDisponibilidad reconstituir(
            Long id,
            Long medicoId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Set<DiaSemana> diasAtencion,
            TimeRange franjaHoraria,
            int duracionCitaMinutos,
            int descansoEntreCitasMinutos
    ) {
        return new PeriodoDisponibilidad(
                id, medicoId, fechaInicio, fechaFin, diasAtencion,
                franjaHoraria, duracionCitaMinutos, descansoEntreCitasMinutos
        );
    }

    @Override
    public boolean incluyeFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha, "fecha es obligatoria");
        if (fecha.isBefore(fechaInicio)) {
            return false;
        }
        return fechaFin == null || !fecha.isAfter(fechaFin);
    }

    @Override
    public boolean atiendeEnDia(DiaSemana dia) {
        return dia != null && diasAtencion.contains(dia);
    }

    /**
     * Cierra el periodo en una fecha concreta. Se usa cuando entra a regir un
     * periodo nuevo para el mismo médico.
     */
    public void cerrarEn(LocalDate fecha) {
        this.fechaFin = validarFechaFin(fechaInicio, requerir(
                fecha, "PERIODO_FECHA_FIN_OBLIGATORIA", "La fecha de cierre del periodo es obligatoria"
        ));
    }

    /** Un periodo sin fecha de fin sigue rigiendo indefinidamente. */
    public boolean estaAbierto() {
        return fechaFin == null;
    }

    /**
     * Clasifica el periodo respecto a una fecha: todavía no empieza, cubre ese
     * día, o ya cerró. Un inicio futuro no es histórico.
     */
    public EstadoVigenciaPeriodo vigenciaEn(LocalDate fecha) {
        Objects.requireNonNull(fecha, "fecha es obligatoria");
        if (fecha.isBefore(fechaInicio)) {
            return EstadoVigenciaPeriodo.FUTURO;
        }
        if (fechaFin != null && fecha.isAfter(fechaFin)) {
            return EstadoVigenciaPeriodo.HISTORICO;
        }
        return EstadoVigenciaPeriodo.VIGENTE;
    }

    /**
     * Detecta el choque de calendarios entre este periodo y otro rango de fechas
     * (usado para impedir dos horarios simultáneos del mismo médico).
     */
    public boolean solapaConRangoFechas(LocalDate otroInicio, LocalDate otroFin) {
        Objects.requireNonNull(otroInicio, "otroInicio es obligatorio");
        boolean terminaAntes = fechaFin != null && fechaFin.isBefore(otroInicio);
        boolean empiezaDespues = otroFin != null && fechaInicio.isAfter(otroFin);
        return !terminaAntes && !empiezaDespues;
    }

    @Override
    public int getPasoMinutos() {
        return duracionCitaMinutos + descansoEntreCitasMinutos;
    }

    @Override
    public boolean cubreRango(TimeRange rango) {
        Objects.requireNonNull(rango, "rango es obligatorio");
        return !rango.getHoraInicio().isBefore(franjaHoraria.getHoraInicio())
                && !rango.getHoraFin().isAfter(franjaHoraria.getHoraFin());
    }

    @Override
    public boolean coincideConRejilla(TimeRange rango) {
        Objects.requireNonNull(rango, "rango es obligatorio");
        if (rango.duracionMinutos() != duracionCitaMinutos) {
            return false;
        }
        int minutosFranja = minutoDelDia(franjaHoraria.getHoraInicio().toSecondOfDay());
        int minutosRango = minutoDelDia(rango.getHoraInicio().toSecondOfDay());
        int desplazamiento = minutosRango - minutosFranja;
        return desplazamiento >= 0 && desplazamiento % getPasoMinutos() == 0;
    }

    @Override
    public Long getMedicoId() {
        return medicoId;
    }

    @Override
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    @Override
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /** Copia inmutable: el conjunto de días no se modifica desde fuera del agregado. */
    public Set<DiaSemana> getDiasAtencion() {
        return Collections.unmodifiableSet(diasAtencion);
    }

    @Override
    public TimeRange getFranjaHoraria() {
        return franjaHoraria;
    }

    @Override
    public int getDuracionCitaMinutos() {
        return duracionCitaMinutos;
    }

    @Override
    public int getDescansoEntreCitasMinutos() {
        return descansoEntreCitasMinutos;
    }

    public Long getId() {
        return id;
    }

    private static int minutoDelDia(int segundoDelDia) {
        return segundoDelDia / 60;
    }

    private static <T> T requerir(T valor, String codigo, String mensaje) {
        if (valor == null) {
            throw ReglaDeNegocioException.de(codigo, mensaje);
        }
        return valor;
    }

    private static Set<DiaSemana> validarDias(Set<DiaSemana> diasAtencion) {
        if (diasAtencion == null || diasAtencion.isEmpty()) {
            throw ReglaDeNegocioException.de(
                    "PERIODO_SIN_DIAS_ATENCION",
                    "Debes seleccionar al menos un día de atención"
            );
        }
        return EnumSet.copyOf(diasAtencion);
    }

    private static int validarDuracion(int duracionCitaMinutos, TimeRange franjaHoraria) {
        if (duracionCitaMinutos < DURACION_CITA_MINIMA_MINUTOS) {
            throw ReglaDeNegocioException.de(
                    "DURACION_CITA_INVALIDA",
                    "La duración de la cita debe ser de al menos " + DURACION_CITA_MINIMA_MINUTOS + " minutos"
            );
        }
        if (duracionCitaMinutos > franjaHoraria.duracionMinutos()) {
            throw ReglaDeNegocioException.de(
                    "DURACION_CITA_INVALIDA",
                    "La duración de la cita (" + duracionCitaMinutos + " min) no cabe en la franja horaria de "
                            + franjaHoraria.duracionMinutos() + " min"
            );
        }
        return duracionCitaMinutos;
    }

    private static int validarDescanso(int descansoEntreCitasMinutos) {
        if (descansoEntreCitasMinutos < 0 || descansoEntreCitasMinutos > DESCANSO_MAXIMO_MINUTOS) {
            throw ReglaDeNegocioException.de(
                    "DESCANSO_ENTRE_CITAS_INVALIDO",
                    "El descanso entre citas debe estar entre 0 y " + DESCANSO_MAXIMO_MINUTOS + " minutos"
            );
        }
        return descansoEntreCitasMinutos;
    }

    private static LocalDate validarFechaFin(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaFin != null && fechaFin.isBefore(fechaInicio)) {
            throw ReglaDeNegocioException.de(
                    "PERIODO_RANGO_FECHAS_INVALIDO",
                    "La fecha de fin del periodo no puede ser anterior a la fecha de inicio"
            );
        }
        return fechaFin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PeriodoDisponibilidad that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
