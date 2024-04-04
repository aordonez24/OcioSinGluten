package com.osc.ociosingluten.modelo;

import herramientas.ExpresionesRegulares;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.ArrayList;

enum Rol{
    COMUN, ADMIN
};

@Entity
@Table (name = "usuarios")
public class Usuario {

    @Id
    @Pattern(regexp= ExpresionesRegulares.DNI)
    private String dni;

    @NotNull
    @Size(min = 0, max = 30)
    private String username;

    @Size(min = 0, max = 15)
    @NotNull
    private String nombre;

    @Size(min = 0, max = 30)
    @NotNull
    private String apellidos;

    @Past
    @NotNull
    private LocalDate fechaNacimiento;

    @Pattern(regexp= ExpresionesRegulares.TLF)
    private int telefono;

    @Lob
    private byte[] fotoPerfil;

    @Email
    private String email;

    @NotNull
    @Pattern(regexp= ExpresionesRegulares.CONTRASENA)
    private String password;

    @ManyToMany
    private ArrayList<Usuario> seguidos;

    @ManyToMany(mappedBy = "seguidos")
    private ArrayList<Usuario> seguidores;

    @ManyToMany
    private ArrayList<Establecimiento> establecimientosFavoritos;

    @ManyToMany
    private ArrayList<Establecimiento> establecimientosVisitados;

    @ManyToMany
    private ArrayList<Actividad> actividades;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Rol rol;

    @OneToMany
    private ArrayList<Comentario> comentariosRealizados;

    @Transient
    private boolean sesionIniciada;
    @Transient
    private boolean sesionCerrada;


    public Usuario(String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, byte[] fotoPerfil, String email, String password) {
        this.dni = dni;
        this.username = username;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.fechaNacimiento = fechaNacimiento;
        this.telefono = telefono;
        this.fotoPerfil = fotoPerfil;
        this.email = email;
        this.password = password;
        this.seguidos = new ArrayList<>();
        this.seguidores = new ArrayList<>();
        this.establecimientosFavoritos = new ArrayList<>();
        this.establecimientosVisitados = new ArrayList<>();
        this.actividades = new ArrayList<>();
        if(comprobarRol(email))
            this.rol = Rol.COMUN;
        else
            this.rol = Rol.ADMIN;
        this.comentariosRealizados = new ArrayList<>();
        this.sesionIniciada = false;
        this.sesionCerrada = true;

    }

    public Usuario() {
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

    public byte[] getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(byte[] fotoPerfil) {
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

    public ArrayList<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(ArrayList<Actividad> contribuciones) {
        this.actividades = contribuciones;
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

    public boolean isSesionIniciada() {
        return sesionIniciada;
    }

    public void setSesionIniciada(boolean sesionIniciada) {
        this.sesionIniciada = sesionIniciada;
    }

    public boolean isSesionCerrada() {
        return sesionCerrada;
    }

    public void setSesionCerrada(boolean sesionCerrada) {
        this.sesionCerrada = sesionCerrada;
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

    public void enviarComentario(Comentario c){
        comentariosRealizados.add(c);
    }
    public void eliminarComentario(Comentario c){
        for(int i=0; i<comentariosRealizados.size(); i++){
            if(comentariosRealizados.get(i) == c){
                comentariosRealizados.remove(i);
            }
        }
    }

    public void anadirEstablecimientoFavorito(Establecimiento est){
        if(!establecimientosFavoritos.contains(est))
            establecimientosFavoritos.add(est);
    }

    public void anadirEstablecimientoVisitado(Establecimiento est, Actividad actividad){
        if(!establecimientosVisitados.contains(est))
            establecimientosVisitados.add(est);
        actividades.add(actividad);
    }

    public void seguirUsuario(Usuario usu){
        if(!seguidos.contains(usu))
            seguidos.add(usu);
    }

    public void eliminarActividad(Actividad cont){
        if(actividades.contains(cont))
            actividades.remove(cont);
    }
    public void anadirActividad(Actividad cont){
        if(!actividades.contains(cont))
            actividades.add(cont);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
