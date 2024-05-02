package com.osc.ociosingluten.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.osc.ociosingluten.herramientas.ExpresionesRegulares;
import com.osc.ociosingluten.herramientas.Rol;
import com.osc.ociosingluten.seguridad.CodificadorPassword;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.osc.ociosingluten.constantes.Constantes.MaxlargoContrasena;

;

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

    @Digits(integer = 9, fraction = 0)
    private int telefono;

    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] fotoPerfil;

    @Email
    private String email;

    @NotNull
    @Pattern(regexp= ExpresionesRegulares.CONTRASENA)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Usuario> seguidos;

    @ManyToMany(mappedBy = "seguidos", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Usuario> seguidores;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Establecimiento> establecimientosFavoritos;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Establecimiento> establecimientosVisitados;

    @ManyToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Actividad> actividades;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Rol rol;

    @OneToMany(fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Comentario> comentariosRealizados;

    @NotNull
    private boolean sesionIniciada;
    @NotNull
    private boolean sesionCerrada;

    @NotNull
    private boolean archivado;


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
        this.archivado = false;
    }


    public Usuario() {
        this.rol = Rol.COMUN;
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

    public List<Usuario> getSeguidos() {
        return seguidos;
    }

    public void setSeguidos(List<Usuario> seguidos) {
        this.seguidos = seguidos;
    }

    public List<Usuario> getSeguidores() {
        return seguidores;
    }

    public void setSeguidores(List<Usuario> seguidores) {
        this.seguidores = seguidores;
    }

    public List<Establecimiento> getEstablecimientosFavoritos() {
        return establecimientosFavoritos;
    }

    public void setEstablecimientosFavoritos(List<Establecimiento> establecimientosFavoritos) {
        this.establecimientosFavoritos = establecimientosFavoritos;
    }

    public List<Establecimiento> getEstablecimientosVisitados() {
        return establecimientosVisitados;
    }

    public void setEstablecimientosVisitados(List<Establecimiento> establecimientosVisitados) {
        this.establecimientosVisitados = establecimientosVisitados;
    }

    public List<Actividad> getActividades() {
        return actividades;
    }

    public void setActividades(List<Actividad> contribuciones) {
        this.actividades = contribuciones;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public List<Comentario> getComentariosRealizados() {
        return comentariosRealizados;
    }

    public void setComentariosRealizados(List<Comentario> comentariosRealizados) {
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

    public void visitarEstablecimiento(Establecimiento est){
        establecimientosVisitados.add(est);
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

    public void olvidarContraseña(Usuario usu){
        //usu es el usuario al que se le olvida la contraseña
        if(usu.isSesionIniciada()){
            usu.setPassword(generarContrasenaAleatoria());
        }
    }

    private String generarContrasenaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int longitud = MaxlargoContrasena;

        StringBuilder contrasena = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < longitud; i++) {
            int indice = random.nextInt(caracteres.length());
            contrasena.append(caracteres.charAt(indice));
        }
        return contrasena.toString();
    }

    public void anadirSeguidor(Usuario usuario){
        if(!seguidores.contains(usuario)){
            seguidores.add(usuario);
        }
    }

    public void dejarSeguir(Usuario usuario){
        seguidos.remove(usuario);
    }

    public void eliminarSeguidor(Usuario usuario){
        seguidos.remove(usuario);
    }

    public boolean isArchivado() {
        return archivado;
    }

    public void setArchivado(boolean archivado) {
        this.archivado = archivado;
    }

    public void anadirComentario(Comentario com){
        comentariosRealizados.add(com);
    }

    public boolean claveValida(String clave){
        return CodificadorPassword.igual(clave, this.password);
    }

}
