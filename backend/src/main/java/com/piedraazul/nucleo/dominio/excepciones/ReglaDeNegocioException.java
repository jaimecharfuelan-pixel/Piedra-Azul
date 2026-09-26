package com.piedraazul.nucleo.dominio.excepciones;

/**
 * Una sola clase para TODAS las reglas de negocio violadas
 * (slot ocupado, médico inactivo, duración inválida, etc.).
 * Se distingue por {@code codigo}, no por subclases.
 */
public final class ReglaDeNegocioException extends DomainException {

    private ReglaDeNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }

    public static ReglaDeNegocioException de(String codigo, String mensaje) {
        return new ReglaDeNegocioException(codigo, mensaje);
    }
}
