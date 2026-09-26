package com.piedraazul.personas.infraestructura.rest;

import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarEspecialidadUseCase;
import com.piedraazul.personas.infraestructura.dto.CrearEspecialidadCommand;
import com.piedraazul.personas.infraestructura.dto.EspecialidadResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/personas/especialidades")
public class EspecialidadController {

    private final GestionarEspecialidadUseCase useCase;

    public EspecialidadController(GestionarEspecialidadUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecialidadResponseDTO crear(@RequestBody CrearEspecialidadCommand comando) {
        return useCase.crear(comando);
    }

    @PutMapping("/{id}/estado")
    public EspecialidadResponseDTO cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean activa = Boolean.TRUE.equals(body.get("activa"));
        return useCase.cambiarEstado(id, activa);
    }

    @GetMapping
    public List<EspecialidadResponseDTO> listarActivas() {
        return useCase.listarActivas();
    }
}