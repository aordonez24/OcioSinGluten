package com.osc.ociosingluten.controlador.DTO;

import java.time.LocalDate;

public class UsuarioDTO {
    private String dni;
    private String username;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private int telefono;
    private byte[] fotoPerfil;
    private String email;
    private String password;

    public String getDni() {
        return dni;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public int getTelefono() {
        return telefono;
    }

    public byte[] getFotoPerfil() {
        return fotoPerfil;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
