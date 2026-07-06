package com.clinicmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicos")
public class Medico extends Persona {
    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(name = "max_citas")
    private int maxCitas;

    public Medico() {
        super();
    }

    public Medico(String id, String nombre, String apellido, int edad, String cedula, String sexo, String especialidad, int maxCitas) {
        super(id, nombre, apellido, edad, cedula, sexo);
        this.especialidad = especialidad;
        this.maxCitas = maxCitas;
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public int getMaxCitas() { return maxCitas; }
    public void setMaxCitas(int maxCitas) { this.maxCitas = maxCitas; }
}
