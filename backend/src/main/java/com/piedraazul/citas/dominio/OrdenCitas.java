package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Arrays;
import java.util.Locale;

/**
 * Criterios de ordenamiento admitidos por el listado del agendador (RF1).
 *
 * <p>El orden por hora y por estado se resuelve en la base de datos (hay índice
 * por {@code medico_id, fecha}); el orden por nombre del paciente se aplica en
 * memoria porque el nombre vive en el módulo Personas.</p>
 */
public enum OrdenCitas {
    HORA_ASC,
    HORA_DESC,
    ESTADO_ASC,
    ESTADO_DESC,
    PACIENTE_ASC,
    PACIENTE_DESC;

    public static final OrdenCitas POR_DEFECTO = HORA_ASC;

    /**
     * Interpreta el valor que llega por query param. Vacío o nulo cae en el orden por defecto.
     */
    public static OrdenCitas desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return POR_DEFECTO;
        }
        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ReglaDeNegocioException.de(
                    "ORDEN_INVALIDO",
                    "Orden no soportado: '" + valor + "'. Valores válidos: " + Arrays.toString(values())
            );
        }
    }

    public boolean esDescendente() {
        return name().endsWith("_DESC");
    }

    public boolean ordenaPorPaciente() {
        return this == PACIENTE_ASC || this == PACIENTE_DESC;
    }

    public boolean ordenaPorEstado() {
        return this == ESTADO_ASC || this == ESTADO_DESC;
    }
}
