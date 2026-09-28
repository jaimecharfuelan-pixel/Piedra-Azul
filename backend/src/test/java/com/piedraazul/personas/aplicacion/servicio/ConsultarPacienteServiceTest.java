package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Paciente;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarPacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @InjectMocks
    private ConsultarPacienteService servicio;

    @Test
    void buscarPorIdDevuelveElDto() {
        when(pacienteRepository.buscarPorId(2L))
                .thenReturn(Optional.of(Paciente.reconstituir(2L, 8L, "María", "3009876543")));

        var dto = servicio.buscarPorId(2L);

        assertEquals("María", dto.nombreCompleto());
        assertEquals("3009876543", dto.telefono());
    }

    @Test
    void buscarPorIdFallaSiNoExiste() {
        when(pacienteRepository.buscarPorId(2L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> servicio.buscarPorId(2L)
        );
        assertEquals("PACIENTE_NO_ENCONTRADO", ex.getCodigo());
    }

    @Test
    void listarTodosMapeaCadaPaciente() {
        when(pacienteRepository.listarTodos()).thenReturn(List.of(
                Paciente.reconstituir(1L, 1L, "Juan", "300"),
                Paciente.reconstituir(2L, 2L, "María", "301")
        ));

        assertEquals(2, servicio.listarTodos().size());
    }
}
