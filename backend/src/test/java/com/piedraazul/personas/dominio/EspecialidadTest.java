package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EspecialidadTest {

    @Test
    void naceActivaYRecortaElNombre() {
        Especialidad especialidad = new Especialidad("  Medicina General  ");

        assertEquals("Medicina General", especialidad.getNombre());
        assertTrue(especialidad.isActiva());
    }

    @Test
    void rechazaNombreVacio() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new Especialidad(" ")
        );
        assertEquals("ESPECIALIDAD_NOMBRE_INVALIDO", ex.getCodigo());
    }

    @Test
    void sePuedeDesactivarYVolverAActivar() {
        Especialidad especialidad = Especialidad.reconstituir(1L, "Fisioterapia", true);

        especialidad.desactivar();
        assertFalse(especialidad.isActiva());
        especialidad.activar();
        assertTrue(especialidad.isActiva());
    }

    @Test
    void laIdentidadEsElIdPersistido() {
        Especialidad a = Especialidad.reconstituir(5L, "A", true);
        Especialidad b = Especialidad.reconstituir(5L, "B", false);

        assertEquals(a, b);
        assertFalse(a.equals(new Especialidad("A")));
    }
}
