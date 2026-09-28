package com.piedraazul.citas.aplicacion.servicio;

import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoMedicosPort;
import com.piedraazul.personas.aplicacion.puertos.salida.CatalogoPacientesPort;
import com.piedraazul.personas.infraestructura.dto.MedicoResumenDTO;
import com.piedraazul.personas.infraestructura.dto.PacienteResumenDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaDtoAssemblerTest {

    @Mock
    private CatalogoMedicosPort catalogoMedicosPort;
    @Mock
    private CatalogoPacientesPort catalogoPacientesPort;

    @Test
    void reutilizaElNombreDelMedicoDentroDelMismoListado() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L))
                .thenReturn(Optional.of(new PacienteResumenDTO(2L, "Juan", "300")));
        CitaDtoAssembler assembler = new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort);

        var filas = assembler.toDtos(List.of(cita(1L), cita(2L)));

        assertEquals("Ana", filas.get(0).medicoNombre());
        assertEquals("Ana", filas.get(1).medicoNombre());
        verify(catalogoMedicosPort, times(1)).obtenerResumen(1L);
    }

    @Test
    void siElPacienteYaNoExisteUsaUnNombreDeRespaldo() {
        when(catalogoMedicosPort.obtenerResumen(1L)).thenReturn(new MedicoResumenDTO(1L, "Ana"));
        when(catalogoPacientesPort.buscarResumenPaciente(2L)).thenReturn(Optional.empty());
        CitaDtoAssembler assembler = new CitaDtoAssembler(catalogoMedicosPort, catalogoPacientesPort);

        var dto = assembler.toDto(cita(1L));

        assertEquals("Paciente #2", dto.pacienteNombre());
        assertEquals("", dto.pacienteTelefono());
    }

    private static Cita cita(Long id) {
        return Cita.reconstituir(
                id, 2L, 1L, LocalDate.of(2026, 9, 29),
                new TimeRange(LocalTime.of(8, 0), LocalTime.of(8, 30)),
                EstadoCita.PROGRAMADA
        );
    }
}
