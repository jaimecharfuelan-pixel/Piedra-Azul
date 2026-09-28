package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.salida.EspecialidadRepository;
import com.piedraazul.personas.dominio.Especialidad;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionarEspecialidadServiceTest {

    @Mock
    private EspecialidadRepository especialidadRepository;
    @InjectMocks
    private GestionarEspecialidadService servicio;

    @Test
    void creaUnaEspecialidadActiva() {
        when(especialidadRepository.guardar(any())).thenAnswer(inv -> {
            Especialidad especialidad = inv.getArgument(0);
            return Especialidad.reconstituir(1L, especialidad.getNombre(), especialidad.isActiva());
        });

        var dto = servicio.crear(new CrearEspecialidadCommand(" Fisioterapia "));

        assertEquals(1L, dto.id());
        assertEquals("Fisioterapia", dto.nombre());
        assertTrue(dto.activa());
    }

    @Test
    void cambiarEstadoDesactivaLaExistente() {
        when(especialidadRepository.buscarPorId(1L))
                .thenReturn(Optional.of(Especialidad.reconstituir(1L, "Fisioterapia", true)));
        when(especialidadRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.cambiarEstado(1L, false);

        assertFalse(dto.activa());
    }

    @Test
    void cambiarEstadoFallaSiNoExiste() {
        when(especialidadRepository.buscarPorId(9L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.cambiarEstado(9L, true)
        );
        assertEquals("ESPECIALIDAD_NO_ENCONTRADA", ex.getCodigo());
    }

    @Test
    void listarActivasDevuelveSoloLoQueEntregaElRepositorio() {
        when(especialidadRepository.listarActivas())
                .thenReturn(List.of(Especialidad.reconstituir(1L, "Medicina", true)));

        assertEquals(1, servicio.listarActivas().size());
    }
}
