package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.aplicacion.puertos.salida.ConsultaRepository;
import com.piedraazul.citas.dominio.Consulta;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarHistorialServiceTest {

    @Mock
    private ConsultaRepository consultaRepository;
    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;

    private ConsultarHistorialService servicio;

    @BeforeEach
    void setUp() {
        servicio = new ConsultarHistorialService(
                consultaRepository,
                new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort)
        );
    }

    @Test
    void listaElHistorialDelPacienteConNombres() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Juan", "300")));
        when(consultaRepository.listarPorPaciente(2L)).thenReturn(List.of(consulta()));

        var historial = servicio.porPaciente(2L);

        assertEquals(1, historial.size());
        assertEquals("Ana", historial.get(0).medicoNombre());
        assertEquals("Control", historial.get(0).observaciones());
    }

    @Test
    void listaElHistorialDelMedico() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Juan", "300")));
        when(consultaRepository.listarPorMedico(1L)).thenReturn(List.of(consulta()));

        assertEquals(2L, servicio.porMedico(1L).get(0).pacienteId());
    }

    private static Consulta consulta() {
        return Consulta.reconstituir(
                9L, 4L, 1L, 2L, LocalDate.of(2026, 9, 28), "Control", LocalDateTime.of(2026, 9, 28, 10, 0)
        );
    }
}
