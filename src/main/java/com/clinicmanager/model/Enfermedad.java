package com.clinicmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "enfermedades")
public class Enfermedad {
    @Id
    @Column(length = 50)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String gravedad; 

    private boolean activo = true;

    public Enfermedad() {}

    public Enfermedad(String id, String nombre, String descripcion, String gravedad) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.gravedad = gravedad;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
