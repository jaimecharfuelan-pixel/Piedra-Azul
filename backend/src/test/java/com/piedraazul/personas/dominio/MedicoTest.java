package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedicoTest {

    @Test
    void naceActivoYRecortaElNombre() {
        Medico medico = new Medico("  Dra. Ana Pérez  ", 3L);

        assertEquals("Dra. Ana Pérez", medico.getNombreCompleto());
        assertEquals(3L, medico.getEspecialidadId());
        assertTrue(medico.estaActivo());
    }

    @Test
    void rechazaNombreVacio() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new Medico("   ", 1L)
        );
        assertEquals("MEDICO_NOMBRE_INVALIDO", ex.getCodigo());
    }

    @Test
    void rechazaEspecialidadAusente() {
        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> new Medico("Ana", null)
        );
        assertEquals("MEDICO_ESPECIALIDAD_OBLIGATORIA", ex.getCodigo());
    }

    @Test
    void activarYDesactivarCambianElEstado() {
        Medico medico = Medico.reconstituir(1L, "Ana", 2L, true);

        medico.desactivar();
        assertFalse(medico.estaActivo());
        medico.activar();
        assertTrue(medico.estaActivo());
    }

    @Test
    void actualizarReemplazaNombreYEspecialidad() {
        Medico medico = new Medico("Ana", 1L);

        medico.actualizar("  Carlos  ", 9L);

        assertEquals("Carlos", medico.getNombreCompleto());
        assertEquals(9L, medico.getEspecialidadId());
    }

    @Test
    void laIdentidadEsElIdPersistido() {
        Medico conId = Medico.reconstituir(4L, "Ana", 1L, true);
        Medico mismo = Medico.reconstituir(4L, "Otro nombre", 2L, false);
        Medico sinId = new Medico("Ana", 1L);

        assertEquals(conId, mismo);
        assertEquals(conId.hashCode(), mismo.hashCode());
        assertFalse(conId.equals(sinId));
    }
}
