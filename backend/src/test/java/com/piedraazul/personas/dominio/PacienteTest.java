package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PacienteTest {

    @Test
    void recortaNombreYTelefono() {
        Paciente paciente = new Paciente(8L, "  Juan Ramírez  ", " 3001234567 ");

        assertEquals(8L, paciente.getUsuarioId());
        assertEquals("Juan Ramírez", paciente.getNombreCompleto());
        assertEquals("3001234567", paciente.getTelefono());
    }

    @Test
    void telefonoNuloQuedaVacio() {
        Paciente paciente = new Paciente(1L, "María", null);
        assertEquals("", paciente.getTelefono());
    }

    @Test
    void rechazaNombreVacio() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new Paciente(1L, "  ", "3001234567")
        );
        assertEquals("PACIENTE_NOMBRE_INVALIDO", ex.getCodigo());
    }

    @Test
    void exigeUsuario() {
        assertThrows(NullPointerException.class, () -> new Paciente(null, "Juan", "300"));
    }

    @Test
    void laIdentidadEsElIdPersistido() {
        Paciente conId = Paciente.reconstituir(2L, 8L, "Juan", "300");
        Paciente mismo = Paciente.reconstituir(2L, 9L, "Otro", "111");

        assertEquals(conId, mismo);
        assertFalse(conId.equals(new Paciente(8L, "Juan", "300")));
    }
}
