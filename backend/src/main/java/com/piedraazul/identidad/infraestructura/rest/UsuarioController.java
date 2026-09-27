package com.piedraazul.identidad.infraestructura.rest;

import com.piedraazul.identidad.aplicacion.puertos.entrada.GestionarUsuarioUseCase;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final GestionarUsuarioUseCase gestionarUsuarioUseCase;

    public UsuarioController(GestionarUsuarioUseCase gestionarUsuarioUseCase) {
        this.gestionarUsuarioUseCase = gestionarUsuarioUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO crear(@Valid @RequestBody CrearUsuarioCommand comando) {
        return gestionarUsuarioUseCase.crear(comando);
    }

    @PutMapping("/{id}/estado")
    public UsuarioResponseDTO cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        boolean activo = Boolean.TRUE.equals(body.get("activo"));
        return gestionarUsuarioUseCase.cambiarEstado(id, activo);
    }

    @GetMapping("/{id}")
    public UsuarioResponseDTO buscarPorId(@PathVariable Long id) {
        return gestionarUsuarioUseCase.buscarPorId(id);
    }

    @GetMapping
    public List<UsuarioResponseDTO> listar() {
        return gestionarUsuarioUseCase.listar();
    }
}
