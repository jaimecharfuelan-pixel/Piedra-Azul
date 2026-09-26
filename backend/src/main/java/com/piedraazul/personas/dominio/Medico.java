package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Objects;

/**
 * Médico/terapeuta del catálogo PiedraAzul (RF1, RF2, RF3).
 */
public class Medico {

    private Long id;
    private String nombreCompleto;
    private Long especialidadId;
    private boolean activo;

    public Medico(String nombreCompleto, Long especialidadId) {
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.especialidadId = validarEspecialidadId(especialidadId);
        this.activo = true;
    }

    private Medico(Long id, String nombreCompleto, Long especialidadId, boolean activo) {
        this.id = id;
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.especialidadId = validarEspecialidadId(especialidadId);
        this.activo = activo;
    }

    public static Medico reconstituir(Long id, String nombreCompleto, Long especialidadId, boolean activo) {
        return new Medico(id, nombreCompleto, especialidadId, activo);
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public boolean estaActivo() {
        return activo;
    }

    public void actualizar(String nombreCompleto, Long especialidadId) {
        this.nombreCompleto = validarNombre(nombreCompleto);
        this.especialidadId = validarEspecialidadId(especialidadId);
    }

    public Long getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public Long getEspecialidadId() {
        return especialidadId;
    }

    void asignarId(Long id) {
        this.id = id;
    }

    private static String validarNombre(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw ReglaDeNegocioException.de("MEDICO_NOMBRE_INVALIDO", "El nombre completo del médico es obligatorio");
        }
        return nombreCompleto.trim();
    }

    private static Long validarEspecialidadId(Long especialidadId) {
        if (especialidadId == null) {
            throw ReglaDeNegocioException.de("MEDICO_ESPECIALIDAD_OBLIGATORIA", "La especialidad del médico es obligatoria");
        }
        return especialidadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Medico that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
