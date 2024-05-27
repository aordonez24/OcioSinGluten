package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Usuario;

import java.time.LocalDate;
import java.util.Base64;



    public record UsuarioDTO(String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, String fotoPerfil, String email, String password, String rol){

    public UsuarioDTO(Usuario usuario) {
        this(usuario.getDni(), usuario.getUsername(), usuario.getNombre(), usuario.getApellidos(), usuario.getFechaNacimiento(), usuario.getTelefono(), Base64.getEncoder().encodeToString(usuario.getFotoPerfil()), usuario.getEmail(), usuario.getPassword(), usuario.getRol().toString());
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

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
