package com.piedraazul.personas.infraestructura.rest;

import com.piedraazul.personas.aplicacion.puertos.entrada.ConsultarPacienteUseCase;
import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personas/pacientes")
public class PacienteController {

    private final ConsultarPacienteUseCase useCase;

    public PacienteController(ConsultarPacienteUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/{id}")
    public PacienteResponseDTO buscarPorId(@PathVariable Long id) {
        return useCase.buscarPorId(id);
    }

    @GetMapping
    public List<PacienteResponseDTO> listarTodos() {
        return useCase.listarTodos();
    }
}
