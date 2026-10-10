package com.piedraazul.personas.aplicacion.servicio;

import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Paciente;
import com.piedraazul.personas.infraestructura.dto.RegistrarPacienteCommand;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarPacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;
    @InjectMocks
    private RegistrarPacienteService servicio;

    @Test
    void guardaElPacienteConUnUsuarioDeRespaldo() {
        when(pacienteRepository.buscarPorCedula(anyString())).thenReturn(Optional.empty());
        when(pacienteRepository.buscarPorUsuarioId(any())).thenReturn(Optional.empty());
        when(pacienteRepository.guardar(any())).thenAnswer(inv -> {
            Paciente paciente = inv.getArgument(0);
            return Paciente.reconstituir(
                    7L,
                    paciente.getUsuarioId(),
                    paciente.getCedula(),
                    paciente.getNombreCompleto(),
                    paciente.getTelefono()
            );
        });

        var dto = servicio.ejecutar(new RegistrarPacienteCommand("1001001001", "Juan Ramírez", "3001234567"));

        assertEquals(7L, dto.id());
        assertEquals("1001001001", dto.cedula());
        assertEquals("Juan Ramírez", dto.nombreCompleto());
        assertEquals("3001234567", dto.telefono());
        assertNotNull(dto);
    }
}
