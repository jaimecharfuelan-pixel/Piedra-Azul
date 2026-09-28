package com.piedraazul.citas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrdenCitasTest {

    @Test
    void vacioUsaElOrdenPorDefecto() {
        assertEquals(OrdenCitas.HORA_ASC, OrdenCitas.desde(null));
        assertEquals(OrdenCitas.HORA_ASC, OrdenCitas.desde("  "));
        assertEquals(OrdenCitas.HORA_ASC, OrdenCitas.POR_DEFECTO);
    }

    @Test
    void aceptaElNombreSinImportarMayusculasNiEspacios() {
        assertEquals(OrdenCitas.PACIENTE_DESC, OrdenCitas.desde(" paciente_desc "));
    }

    @Test
    void rechazaUnCriterioDesconocido() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> OrdenCitas.desde("por_color")
        );
        assertEquals("ORDEN_INVALIDO", ex.getCodigo());
    }

    @Test
    void distingueDescendentePacienteYEstado() {
        assertTrue(OrdenCitas.HORA_DESC.esDescendente());
        assertFalse(OrdenCitas.HORA_ASC.esDescendente());
        assertTrue(OrdenCitas.PACIENTE_ASC.ordenaPorPaciente());
        assertFalse(OrdenCitas.ESTADO_ASC.ordenaPorPaciente());
        assertTrue(OrdenCitas.ESTADO_DESC.ordenaPorEstado());
        assertFalse(OrdenCitas.PACIENTE_DESC.ordenaPorEstado());
    }
}
