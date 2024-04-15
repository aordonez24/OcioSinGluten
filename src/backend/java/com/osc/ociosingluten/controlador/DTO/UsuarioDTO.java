package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Usuario;

import java.time.LocalDate;


    public record UsuarioDTO(String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, byte[] fotoPerfil, String email, String password){

    public UsuarioDTO(Usuario usuario) {
        this(usuario.getDni(), usuario.getUsername(), usuario.getNombre(), usuario.getApellidos(), usuario.getFechaNacimiento(), usuario.getTelefono(), usuario.getFotoPerfil(), usuario.getEmail(), usuario.getPassword());
    }

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
