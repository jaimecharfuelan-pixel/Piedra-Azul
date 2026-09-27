package com.piedraazul.disponibilidad.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.time.LocalDate;

/**
 * Parámetro GLOBAL del sistema (una sola fila): cuántas semanas a futuro se
 * puede agendar. No depende del médico (RF3).
 */
public class ConfiguracionSistema {

    /** La tabla de configuración tiene una única fila con este id. */
    public static final Long ID_UNICO = 1L;

    public static final int VENTANA_MINIMA_SEMANAS = 1;
    public static final int VENTANA_MAXIMA_SEMANAS = 52;
    public static final int VENTANA_POR_DEFECTO_SEMANAS = 4;

    private Long id;
    private int ventanaSemanas;

    private ConfiguracionSistema(Long id, int ventanaSemanas) {
        this.id = id;
        this.ventanaSemanas = validarVentana(ventanaSemanas);
    }

    /**
     * Valor inicial usado cuando todavía no se ha configurado nada.
     */
    public static ConfiguracionSistema porDefecto() {
        return new ConfiguracionSistema(ID_UNICO, VENTANA_POR_DEFECTO_SEMANAS);
    }

    public static ConfiguracionSistema reconstituir(Long id, int ventanaSemanas) {
        return new ConfiguracionSistema(id, ventanaSemanas);
    }

    public void actualizarVentana(int semanas) {
        this.ventanaSemanas = validarVentana(semanas);
    }

    public int getVentanaSemanas() {
        return ventanaSemanas;
    }

    public Long getId() {
        return id;
    }

    /**
     * Última fecha que un paciente puede elegir al agendar, contada desde {@code hoy}.
     */
    public LocalDate ultimaFechaAgendable(LocalDate hoy) {
        return hoy.plusWeeks(ventanaSemanas);
    }

    /**
     * Regla del RF3: solo se agenda desde hoy y hasta el final de la ventana configurada.
     */
    public boolean permiteAgendarEn(LocalDate fecha, LocalDate hoy) {
        return !fecha.isBefore(hoy) && !fecha.isAfter(ultimaFechaAgendable(hoy));
    }

    private static int validarVentana(int semanas) {
        if (semanas < VENTANA_MINIMA_SEMANAS || semanas > VENTANA_MAXIMA_SEMANAS) {
            throw ReglaDeNegocioException.de(
                    "VENTANA_AGENDAMIENTO_INVALIDA",
                    "La ventana de agendamiento debe estar entre " + VENTANA_MINIMA_SEMANAS
                            + " y " + VENTANA_MAXIMA_SEMANAS + " semanas"
            );
        }
        return semanas;
    }
}
