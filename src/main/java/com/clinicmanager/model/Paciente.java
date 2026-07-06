package com.clinicmanager.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pacientes")
public class Paciente extends Persona {
    private float peso;
    private float estatura;

    @Column(name = "tipo_sangre", length = 10)
    private String tipoSangre;

    @Column(length = 255)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "paciente_vacunas",
        joinColumns = @JoinColumn(name = "paciente_id"),
        inverseJoinColumns = @JoinColumn(name = "vacuna_id")
    )
    private List<Vacuna> vacunas = new ArrayList<>();

    public Paciente() {
        super();
    }

    public Paciente(String id, String nombre, String apellido, int edad, String cedula, String sexo,
                    float peso, float estatura, String tipoSangre, String direccion, String telefono) {
        super(id, nombre, apellido, edad, cedula, sexo);
        this.peso = peso;
        this.estatura = estatura;
        this.tipoSangre = tipoSangre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.vacunas = new ArrayList<>();
    }

    public float getPeso() { return peso; }
    public void setPeso(float peso) { this.peso = peso; }

    public float getEstatura() { return estatura; }
    public void setEstatura(float estatura) { this.estatura = estatura; }

    public String getTipoSangre() { return tipoSangre; }
    public void setTipoSangre(String tipoSangre) { this.tipoSangre = tipoSangre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public List<Vacuna> getVacunas() { return vacunas; }
    public void setVacunas(List<Vacuna> vacunas) { this.vacunas = vacunas; }

    public void agregarVacuna(Vacuna vacuna) {
        if (!this.vacunas.contains(vacuna)) {
            this.vacunas.add(vacuna);
        }
    }
}
