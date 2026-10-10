package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Objects;

/**
 * Paciente vinculado a un usuario del módulo Identidad (RF2).
 */
public class Paciente {

    private Long id;
    private Long usuarioId;
    private String cedula;
    private String nombreCompleto;
    private String telefono;

    public Paciente(Long usuarioId, String cedula, String nombreCompleto, String telefono) {
        this.usuarioId = Objects.requireNonNull(usuarioId, "usuarioId es obligatorio");
        this.cedula = validarCedula(cedula);
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    private Paciente() {
    }

    /**
     * Reconstitución desde persistencia: tolera cédula nula en filas antiguas migradas.
     */
    public static Paciente reconstituir(Long id, Long usuarioId, String cedula, String nombreCompleto, String telefono) {
        Paciente paciente = new Paciente();
        paciente.id = id;
        paciente.usuarioId = usuarioId;
        paciente.cedula = cedula == null || cedula.isBlank()
                ? null
                : cedula.trim().replaceAll("[.\\s-]", "");
        paciente.nombreCompleto = validarNombre(nombreCompleto);
        paciente.telefono = telefono == null ? "" : telefono.trim();
        return paciente;
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getCedula() {
        return cedula;
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

    private static String validarCedula(String cedula) {
        if (cedula == null || cedula.isBlank()) {
            throw ReglaDeNegocioException.de("PACIENTE_CEDULA_INVALIDA", "La cédula del paciente es obligatoria");
        }
        String normalizada = cedula.trim().replaceAll("[.\\s-]", "");
        if (!normalizada.matches("\\d{6,15}")) {
            throw ReglaDeNegocioException.de(
                    "PACIENTE_CEDULA_INVALIDA",
                    "La cédula debe tener entre 6 y 15 dígitos"
            );
        }
        return normalizada;
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
