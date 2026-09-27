package com.piedraazul.identidad.aplicacion.puertos.entrada;

import com.piedraazul.identidad.infraestructura.dto.CrearUsuarioCommand;
import com.piedraazul.identidad.infraestructura.dto.UsuarioResponseDTO;

import java.util.List;

public interface GestionarUsuarioUseCase {

    UsuarioResponseDTO crear(CrearUsuarioCommand comando);

    UsuarioResponseDTO cambiarEstado(Long id, boolean activo);

    UsuarioResponseDTO buscarPorId(Long id);

    List<UsuarioResponseDTO> listar();
}
