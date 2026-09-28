package com.piedraazul.nucleo.dominio.excepciones;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ExcepcionesDeDominioTest {

    @Test
    void laReglaDeNegocioConservaCodigoYMensaje() {
        ReglaDeNegocioException ex = ReglaDeNegocioException.de("SLOT_NO_DISPONIBLE", "Franja tomada");

        assertEquals("SLOT_NO_DISPONIBLE", ex.getCodigo());
        assertEquals("Franja tomada", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void elRecursoNoEncontradoConservaCodigoYMensaje() {
        RecursoNoEncontradoException ex = RecursoNoEncontradoException.de("CITA_NO_ENCONTRADA", "No existe");

        assertEquals("CITA_NO_ENCONTRADA", ex.getCodigo());
        assertEquals("No existe", ex.getMessage());
        assertInstanceOf(DomainException.class, ex);
    }
}
