package com.clinicmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vacunas")
public class Vacuna {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String fabricante;

    private float dosis;

    @Column(length = 500)
    private String descripcion;

    private boolean activo = true;

    public Vacuna() {}

    public Vacuna(String id, String nombre, String fabricante, float dosis, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.fabricante = fabricante;
        this.dosis = dosis;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFabricante() { return fabricante; }
    public void setFabricante(String fabricante) { this.fabricante = fabricante; }

    public float getDosis() { return dosis; }
    public void setDosis(float dosis) { this.dosis = dosis; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
