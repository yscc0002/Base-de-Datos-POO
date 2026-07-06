package com.clinicmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Persona {
    @Id
    @Column(length = 50)
    protected String id;

    @Column(unique = true, nullable = false, length = 20)
    protected String cedula;

    @Column(nullable = false, length = 100)
    protected String nombre;

    @Column(nullable = false, length = 100)
    protected String apellido;

    protected int edad;

    @Column(length = 10)
    protected String sexo;

    protected boolean activo = true;

    public Persona() {}

    public Persona(String id, String nombre, String apellido, int edad, String cedula, String sexo) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.cedula = cedula;
        this.sexo = sexo;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
