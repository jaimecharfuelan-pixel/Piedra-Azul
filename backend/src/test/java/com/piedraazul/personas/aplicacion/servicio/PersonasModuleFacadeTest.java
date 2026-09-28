package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.nucleo.dominio.excepciones.RecursoNoEncontradoException;
import com.piedraazul.personas.aplicacion.puertos.salida.MedicoRepository;
import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Medico;
import com.piedraazul.personas.dominio.Paciente;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonasModuleFacadeTest {

    @Mock
    private MedicoRepository medicoRepository;
    @Mock
    private PacienteRepository pacienteRepository;
    @InjectMocks
    private PersonasModuleFacade fachada;

    @Test
    void existeMedicoActivoSoloSiEstaActivo() {
        when(medicoRepository.buscarPorId(1L)).thenReturn(Optional.of(Medico.reconstituir(1L, "Ana", 2L, true)));
        when(medicoRepository.buscarPorId(2L)).thenReturn(Optional.of(Medico.reconstituir(2L, "Luis", 2L, false)));
        when(medicoRepository.buscarPorId(3L)).thenReturn(Optional.empty());

        assertTrue(fachada.existeMedicoActivo(1L));
        assertFalse(fachada.existeMedicoActivo(2L));
        assertFalse(fachada.existeMedicoActivo(3L));
    }

    @Test
    void obtenerResumenFallaSiNoHayMedico() {
        when(medicoRepository.buscarPorId(1L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> fachada.obtenerResumen(1L)
        );
        assertEquals("MEDICO_NO_ENCONTRADO", ex.getCodigo());
    }

    @Test
    void existePacienteRechazaIdNulo() {
        assertFalse(fachada.existePaciente(null));
    }

    @Test
    void buscarResumenPacienteVacioSiElIdEsNulo() {
        assertTrue(fachada.buscarResumenPaciente(null).isEmpty());
    }

    @Test
    void creaMedicoOPacienteSegunElRolYNadaParaElAdministrador() {
        when(medicoRepository.guardar(any())).thenReturn(Medico.reconstituir(11L, "Ana", 2L, true));
        when(pacienteRepository.guardar(any())).thenReturn(Paciente.reconstituir(12L, 4L, "Juan", "300"));

        assertEquals(11L, fachada.crearPersonaParaUsuario(
                RolUsuario.MEDICO, new DatosPersonaDTO(4L, "Ana", null, 2L)));
        assertEquals(12L, fachada.crearPersonaParaUsuario(
                RolUsuario.PACIENTE, new DatosPersonaDTO(4L, "Juan", "300", null)));
        assertNull(fachada.crearPersonaParaUsuario(RolUsuario.ADMINISTRADOR, null));
    }
}
