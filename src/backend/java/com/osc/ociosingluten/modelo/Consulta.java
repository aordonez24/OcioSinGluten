package com.osc.ociosingluten.modelo;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "quejas")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // Generar ID automáticamente
    private int idQueja;

    @NotNull
    private String nombreUsuario;
    @Email
    private String email;

    @Size(min = 0, max = 280)
    private String mensaje;

    public Consulta(String nombreUsuario, String email, String mensaje) {
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.mensaje = mensaje;
    }

    public Consulta() {

    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getIdQueja() {
        return idQueja;
    }
}