package com.piedraazul.identidad.aplicacion.puertos.salida;

import com.piedraazul.nucleo.dominio.RolUsuario;

/**
 * Puerto hacia el proveedor externo de identidad (Keycloak en Docker;
 * emisor local solo para pruebas sin contenedor).
 */
public interface ProveedorIdentidadPort {

    record TokenProveedor(String accessToken, String tokenType, long expiresInSeconds) {
    }

    TokenProveedor autenticar(String username, String passwordPlano);

    /** Crea el usuario en el IdP si no existe (idempotente). */
    void asegurarUsuario(String username, String passwordPlano, RolUsuario rol);

    void sincronizarEstado(String username, boolean activo);
}
