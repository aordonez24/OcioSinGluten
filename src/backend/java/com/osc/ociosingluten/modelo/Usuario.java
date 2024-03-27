package com.osc.ociosingluten.modelo;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;

enum Rol{
    COMUN, ADMIN
};
public class Usuario {
    private String username;
    private String nombre;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private int telefono;
    private Image fotoPerfil;
    private String email;
    private String password;
    private ArrayList<Usuario> seguidos;
    private ArrayList<Usuario> seguidores;
    private ArrayList<Establecimiento> establecimientosFavoritos;
    private ArrayList<Establecimiento> establecimientosVisitados;
    private ArrayList<Contribucion> contribuciones;
    private Rol rol;
    private ArrayList<Comentario> comentariosRealizados;


    public Usuario(String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, Image fotoPerfil, String email, String password) {
        this.username = username;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.fotoPerfil = fotoPerfil;
        this.email = email;
        this.password = hashContrasena(password);
        this.seguidos = new ArrayList<>();
        this.seguidores = new ArrayList<>();
        this.establecimientosFavoritos = new ArrayList<>();
        this.establecimientosVisitados = new ArrayList<>();
        this.contribuciones = new ArrayList<>();
        this.rol = Rol.COMUN;

    }

    private String hashContrasena(String password){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte b : hashBytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean iniciarSesion(String identificador, String password){
        if(identificador.equals(this.email) || identificador.equals(this.username))
            return this.password.equals(hashContrasena(password));
        else
            return false;
    }
}
