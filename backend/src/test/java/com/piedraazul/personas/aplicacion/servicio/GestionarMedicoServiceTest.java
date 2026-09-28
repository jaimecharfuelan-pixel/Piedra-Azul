package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;
import com.piedraazul.personas.aplicacion.puertos.salida.EspecialidadRepository;
import com.piedraazul.personas.aplicacion.puertos.salida.MedicoRepository;
import com.piedraazul.personas.dominio.Especialidad;
import com.piedraazul.personas.dominio.Medico;
import com.piedraazul.personas.infraestructura.dto.ActualizarMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.CrearMedicoCommand;
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
class GestionarMedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private EspecialidadRepository especialidadRepository;
    @InjectMocks
    private GestionarMedicoService servicio;

    @Test
    void creaUnMedicoActivoEnUnaEspecialidadActiva() {
        when(especialidadRepository.buscarPorId(3L)).thenReturn(Optional.of(especialidad(3L, true)));
        when(medicoRepository.guardar(any())).thenAnswer(inv -> {
            Medico medico = inv.getArgument(0);
            return Medico.reconstituir(10L, medico.getNombreCompleto(), medico.getEspecialidadId(), medico.estaActivo());
        });

        var dto = servicio.crear(new CrearMedicoCommand(" Ana ", 3L));

        assertEquals(10L, dto.id());
        assertEquals("Ana", dto.nombreCompleto());
        assertTrue(dto.activo());
        assertEquals("Medicina", dto.especialidad().nombre());
    }

    @Test
    void noAsignaUnaEspecialidadInactiva() {
        when(especialidadRepository.buscarPorId(3L)).thenReturn(Optional.of(especialidad(3L, false)));

        ReglaDeNegocioException ex = assertThrows(
                ReglaDeNegocioException.class,
                () -> servicio.crear(new CrearMedicoCommand("Ana", 3L))
        );
        assertEquals("ESPECIALIDAD_INACTIVA", ex.getCodigo());
    }

    @Test
    void actualizarCambiaNombreYEspecialidad() {
        when(medicoRepository.buscarPorId(10L)).thenReturn(Optional.of(Medico.reconstituir(10L, "Ana", 3L, true)));
        when(especialidadRepository.buscarPorId(4L)).thenReturn(Optional.of(especialidad(4L, true)));
        when(medicoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.actualizar(10L, new ActualizarMedicoCommand("Carlos", 4L));

        assertEquals("Carlos", dto.nombreCompleto());
        assertEquals(4L, dto.especialidad().id());
    }

    @Test
    void cambiarEstadoDesactiva() {
        when(medicoRepository.buscarPorId(10L)).thenReturn(Optional.of(Medico.reconstituir(10L, "Ana", 3L, true)));
        when(especialidadRepository.buscarPorId(3L)).thenReturn(Optional.of(especialidad(3L, true)));
        when(medicoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = servicio.cambiarEstado(10L, false);

        assertFalse(dto.activo());
    }

    @Test
    void buscarPorIdFallaSiNoExiste() {
        when(medicoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.buscarPorId(99L)
        );
        assertEquals("MEDICO_NO_ENCONTRADO", ex.getCodigo());
    }

    @Test
    void listarActivosToleraUnaEspecialidadBorrada() {
        when(medicoRepository.listarActivos()).thenReturn(List.of(Medico.reconstituir(10L, "Ana", 3L, true)));
        when(especialidadRepository.buscarPorId(3L)).thenReturn(Optional.empty());

        var lista = servicio.listarActivos();

        assertEquals(1, lista.size());
        assertEquals(null, lista.get(0).especialidad());
    }

    private static Especialidad especialidad(Long id, boolean activa) {
        return Especialidad.reconstituir(id, "Medicina", activa);
    }
}
