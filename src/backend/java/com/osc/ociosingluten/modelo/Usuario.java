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
        if(comprobarRol(email))
            this.rol = Rol.COMUN;
        else
            this.rol = Rol.ADMIN;
        this.comentariosRealizados = new ArrayList<>();

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public Image getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(Image fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Usuario> getSeguidos() {
        return seguidos;
    }

    public void setSeguidos(ArrayList<Usuario> seguidos) {
        this.seguidos = seguidos;
    }

    public ArrayList<Usuario> getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(ArrayList<Usuario> seguidores) {
        this.seguidores = seguidores;
    }

    public ArrayList<Establecimiento> getEstablecimientosFavoritos() {
        return establecimientosFavoritos;
    }

    public void setEstablecimientosFavoritos(ArrayList<Establecimiento> establecimientosFavoritos) {
        this.establecimientosFavoritos = establecimientosFavoritos;
    }

    public ArrayList<Establecimiento> getEstablecimientosVisitados() {
        return establecimientosVisitados;
    }

    public void setEstablecimientosVisitados(ArrayList<Establecimiento> establecimientosVisitados) {
        this.establecimientosVisitados = establecimientosVisitados;
    }

    public ArrayList<Contribucion> getContribuciones() {
        return contribuciones;
    }

    public void setContribuciones(ArrayList<Contribucion> contribuciones) {
        this.contribuciones = contribuciones;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public ArrayList<Comentario> getComentariosRealizados() {
        return comentariosRealizados;
    }

    public void setComentariosRealizados(ArrayList<Comentario> comentariosRealizados) {
        this.comentariosRealizados = comentariosRealizados;
    }

    private static boolean comprobarRol(String email){
        if(email.contains("@")){
            String[] parteCorreo = email.split("@");
            if(parteCorreo.length == 2){
                String dominio = parteCorreo[1];
                return !dominio.contains("admin");
            }
        }
        return true;
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

    public void guardarComentario(Comentario c){
        comentariosRealizados.add(c);
    }



}
