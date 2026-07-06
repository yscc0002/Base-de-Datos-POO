package com.clinicmanager.model;

import jakarta.persistence.*;

@Entity
@Table(name = "consultas")
public class Consulta {
    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enfermedad_id", nullable = true)
    private Enfermedad enfermedadVigilada;

    @Column(name = "es_importante")
    private boolean esImportante;

    public Consulta() {}

    public Consulta(String id, Paciente paciente, Medico medico, String sintomas, String diagnostico,
                    Enfermedad enfermedadVigilada, boolean esImportante) {
        this.id = id;
        this.paciente = paciente;
        this.medico = medico;
        this.sintomas = sintomas;
        this.diagnostico = diagnostico;
        this.enfermedadVigilada = enfermedadVigilada;
        this.esImportante = esImportante;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public String getSintomas() { return sintomas; }
    public void setSintomas(String sintomas) { this.sintomas = sintomas; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public Enfermedad getEnfermedadVigilada() { return enfermedadVigilada; }
    public void setEnfermedadVigilada(Enfermedad enfermedadVigilada) { this.enfermedadVigilada = enfermedadVigilada; }

    public boolean isEsImportante() { return esImportante; }
    public void setEsImportante(boolean esImportante) { this.esImportante = esImportante; }
}
