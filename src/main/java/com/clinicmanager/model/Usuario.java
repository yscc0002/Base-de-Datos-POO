package com.clinicmanager.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @Column(name = "nombre_usuario", length = 50)
    private String nombreUsuario;

    @Column(nullable = false, length = 255)
    private String clave;

    @Column(nullable = false, length = 20)
    private String rol; 

    @Column(name = "link_id", length = 50)
    private String linkId; 

    @Column(name = "es_activo")
    private boolean esActivo = true;

    public Usuario() {}

    public Usuario(String nombreUsuario, String clave, String rol, String linkId) {
        this.nombreUsuario = nombreUsuario;
        this.clave = clave;
        this.rol = rol;
        this.linkId = linkId;
        this.esActivo = true;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getLinkId() { return linkId; }
    public void setLinkId(String linkId) { this.linkId = linkId; }

    public boolean getEsActivo() { return esActivo; }
    public void setEsActivo(boolean esActivo) { this.esActivo = esActivo; }
}
