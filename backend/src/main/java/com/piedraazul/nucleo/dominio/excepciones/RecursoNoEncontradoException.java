package com.piedraazul.nucleo.dominio.excepciones;

/**
 * Recurso de dominio inexistente (médico, paciente, cita, etc.).
 */
public final class RecursoNoEncontradoException extends DomainException {

    private RecursoNoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static RecursoNoEncontradoException de(String codigo, String mensaje) {
        return new RecursoNoEncontradoException(codigo, mensaje);
    }
}
