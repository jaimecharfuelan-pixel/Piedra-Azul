package com.piedraazul.identidad.aplicacion.puertos.salida;

import com.piedraazul.identidad.dominio.Usuario;

public interface JwtTokenPort {

    String generarToken(Usuario usuario);

    String extraerUsername(String token);

    boolean esTokenValido(String token, Usuario usuario);
}
