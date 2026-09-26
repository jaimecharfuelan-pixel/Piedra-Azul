package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Objects;

/**
 * Paciente vinculado a un usuario del módulo Identidad (RF2).
 */
public class Paciente {

    private Long id;
    private Long usuarioId;
    private String nombreCompleto;
    private String telefono;

    public Paciente(Long usuarioId, String nombreCompleto, String telefono) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId es obligatorio");
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    private Paciente(Long id, Long usuarioId, String nombreCompleto, String telefono) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    public static Paciente reconstituir(Long id, Long usuarioId, String nombreCompleto, String telefono) {
        return new Paciente(id, usuarioId, nombreCompleto, telefono);
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    void asignarId(Long id) {
        this.id = id;
    }

    private static String validarNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw ReglaDeNegocioException.de("PACIENTE_NOMBRE_INVALIDO", "El nombre completo del paciente es obligatorio");
        }
        return nombreCompleto.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Paciente that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
