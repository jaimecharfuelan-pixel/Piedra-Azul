package com.piedraazul.nucleo.dominio.excepciones;

/**
 * Raíz de las excepciones de dominio del monolito modular.
 */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
