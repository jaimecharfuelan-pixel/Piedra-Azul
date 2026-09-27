package com.piedraazul.personas.infraestructura.rest;

import com.piedraazul.personas.aplicacion.puertos.entrada.ConsultarPacienteUseCase;
import com.piedraazul.personas.aplicacion.puertos.entrada.RegistrarPacienteUseCase;
import com.piedraazul.personas.infraestructura.dto.PacienteResponseDTO;
import com.piedraazul.personas.infraestructura.dto.RegistrarPacienteCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personas/pacientes")
public class PacienteController {

    private final ConsultarPacienteUseCase consultarUseCase;
    private final RegistrarPacienteUseCase registrarUseCase;

    public PacienteController(
            ConsultarPacienteUseCase consultarUseCase,
            RegistrarPacienteUseCase registrarUseCase
    ) {
        this.consultarUseCase = consultarUseCase;
        this.registrarUseCase = registrarUseCase;
    }

    /**
     * RF2: registro del paciente en la web, previo al agendamiento.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponseDTO registrar(@Valid @RequestBody RegistrarPacienteCommand comando) {
        return registrarUseCase.ejecutar(comando);
    }

    @GetMapping("/{id}")
    public PacienteResponseDTO buscarPorId(@PathVariable Long id) {
        return consultarUseCase.buscarPorId(id);
    }

    @GetMapping
    public List<PacienteResponseDTO> listarTodos() {
        return consultarUseCase.listarTodos();
    }
}
