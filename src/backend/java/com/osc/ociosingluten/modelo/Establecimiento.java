package com.osc.ociosingluten.modelo;

import java.util.ArrayList;

import static com.osc.ociosingluten.constantes.Constantes.mediaValoracion;

public class Establecimiento {
    private String nombre;
    private int telefono;
    private String direccion;
    private int numLikes;
    private ArrayList<Comentario> comentarios;

    public Establecimiento(String nombre, int telefono, String direccion) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.numLikes = 0;
        this.comentarios = new ArrayList<>();
    }

    public Establecimiento(String nombre, int telefono, String direccion, int numLikes, ArrayList<Comentario> comentarios) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.numLikes = numLikes;
        this.comentarios = comentarios;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public ArrayList<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(ArrayList<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public double obtenerValoracion(Establecimiento est){
        return (double) est.getNumLikes()/mediaValoracion;
    }

    public void anadirComentario(Comentario comentario){comentarios.add(comentario);}
}
