package com.piedraazul.personas.infraestructura.rest;

import com.piedraazul.personas.aplicacion.puertos.entrada.GestionarMedicoUseCase;
import com.piedraazul.personas.infraestructura.dto.ActualizarMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.CrearMedicoCommand;
import com.piedraazul.personas.infraestructura.dto.MedicoResponseDTO;
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
@RequestMapping("/api/personas/medicos")
public class MedicoController {

    private final GestionarMedicoUseCase useCase;

    public MedicoController(GestionarMedicoUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicoResponseDTO crear(@RequestBody CrearMedicoCommand comando) {
        return useCase.crear(comando);
    }

    @PutMapping("/{id}")
    public MedicoResponseDTO actualizar(@PathVariable Long id, @RequestBody ActualizarMedicoCommand comando) {
        return useCase.actualizar(id, comando);
    }

    @PutMapping("/{id}/estado")
    public MedicoResponseDTO cambiarEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        boolean activo = Boolean.TRUE.equals(body.get("activo"));
        return useCase.cambiarEstado(id, activo);
    }

    @GetMapping("/{id}")
    public MedicoResponseDTO buscarPorId(@PathVariable Long id) {
        return useCase.buscarPorId(id);
    }

    @GetMapping
    public List<MedicoResponseDTO> listarActivos() {
        return useCase.listarActivos();
    }
}
