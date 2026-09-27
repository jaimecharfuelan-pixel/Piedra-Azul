package com.piedraazul.identidad.aplicacion.puertos.salida;

import com.piedraazul.identidad.dominio.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(Long id);

    Optional<Usuario> buscarPorUsername(String username);

    boolean existePorUsername(String username);

    List<Usuario> listarTodos();
}
