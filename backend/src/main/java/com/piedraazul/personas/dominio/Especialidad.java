package com.piedraazul.personas.dominio;

import com.piedraazul.nucleo.dominio.excepciones.ReglaDeNegocioException;

import java.util.Objects;

/**
 * Especialidad médica (catálogo del módulo Personas).
 */
public class Especialidad {

    private Long id;
    private String nombre;
    private boolean activa;

    public Especialidad(String nombre) {
        this.nombre = validarNombre(nombre);
        this.activa = true;
    }

    private Especialidad(Long id, String nombre, boolean activa) {
        this.id = id;
        this.nombre = validarNombre(nombre);
        this.activa = activa;
    }

    public static Especialidad reconstituir(Long id, String nombre, boolean activa) {
        return new Especialidad(id, nombre, activa);
    }

    public void activar() {
        this.activa = true;
    }

    public void desactivar() {
        this.activa = false;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActiva() {
        return activa;
    }

    void asignarId(Long id) {
        this.id = id;
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw ReglaDeNegocioException.de("ESPECIALIDAD_NOMBRE_INVALIDO", "El nombre de la especialidad es obligatorio");
        }
        return nombre.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Especialidad that) || id == null || that.id == null) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
