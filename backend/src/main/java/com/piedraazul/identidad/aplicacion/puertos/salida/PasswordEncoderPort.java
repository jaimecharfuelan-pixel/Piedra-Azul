package com.piedraazul.identidad.aplicacion.puertos.salida;

public interface PasswordEncoderPort {

    String codificar(String passwordPlano);

    boolean coincide(String passwordPlano, String hash);
}
