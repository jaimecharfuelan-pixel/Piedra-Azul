package com.piedraazul.disponibilidad.infraestructura.persistencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla de una sola fila (id = 1) con los parámetros globales del sistema.
 * El id es asignado, no autogenerado, justamente para que no pueda haber dos filas.
 */
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistemaJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ventana_semanas", nullable = false)
    private int ventanaSemanas;

    protected ConfiguracionSistemaJpaEntity() {
    }

    public ConfiguracionSistemaJpaEntity(Long id, int ventanaSemanas) {
        this.id = id;
        this.ventanaSemanas = ventanaSemanas;
    }

    public Long getId() {
        return id;
    }

    public int getVentanaSemanas() {
        return ventanaSemanas;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setVentanaSemanas(int ventanaSemanas) {
        this.ventanaSemanas = ventanaSemanas;
    }
}
