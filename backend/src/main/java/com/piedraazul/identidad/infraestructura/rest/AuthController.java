package com.piedraazul.identidad.infraestructura.rest;

import com.piedraazul.identidad.aplicacion.puertos.entrada.AutenticarUsuarioUseCase;
import com.piedraazul.identidad.aplicacion.puertos.entrada.GestionarUsuarioUseCase;
import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.LoginRequestDTO;
import com.piedraazul.identidad.infraestructura.dto.RegistrarPacienteRequestDTO;
import com.piedraazul.identidad.infraestructura.dto.TokenResponseDTO;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;
import com.piedraazul.nucleo.dominio.RolUsuario;
import com.piedraazul.personas.infraestructura.dto.DatosPersonaDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final GestionarUsuarioUseCase gestionarUsuarioUseCase;

    public AuthController(
            AutenticarUsuarioUseCase autenticarUsuarioUseCase,
            GestionarUsuarioUseCase gestionarUsuarioUseCase
    ) {
        this.autenticarUsuarioUseCase = autenticarUsuarioUseCase;
        this.gestionarUsuarioUseCase = gestionarUsuarioUseCase;
    }

    @PostMapping("/login")
    public TokenResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return autenticarUsuarioUseCase.ejecutar(request.username(), request.password());
    }

    @PostMapping("/registro-paciente")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponseDTO registrarPaciente(@Valid @RequestBody RegistrarPacienteRequestDTO request) {
        return gestionarUsuarioUseCase.crear(new CrearUsuarioCommand(
                request.username(),
                request.password(),
                RolUsuario.PACIENTE,
                new DatosPersonaDTO(null, request.nombreCompleto(), request.telefono(), null)
        ));
    }
}
