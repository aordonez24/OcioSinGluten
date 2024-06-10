package com.osc.ociosingluten.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // Generar ID automáticamente
    private int id;

    @ManyToOne
    @JoinColumn(name = "username") // Nombre de la columna en la tabla de Comentario que hace referencia al usuario
    private Usuario autor;

    @NotNull
    private LocalDate fecha;

    @Size(max = 140)
    @NotNull
    private String mensaje;

    private int numLikes;

    @ManyToMany
    @JsonIgnore
    private List<Usuario> usuariosQueDieronLike;

    public Comentario(String mensaje, Usuario autor) {
        this.mensaje = mensaje;
        this.autor = autor;
        this.numLikes = 0;
        this.fecha = LocalDate.now();
    }

    public Comentario() {

    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNumLikes(int numLikes, Usuario usuario, int modo) {
        this.numLikes = numLikes;
        if(modo == 1){
            this.getUsuariosQueDieronLike().add(usuario);
        }else{
            this.getUsuariosQueDieronLike().remove(usuario);
        }
    }

    public void sumarLike() {
        this.numLikes += 1;
    }

    public List<Usuario> getUsuariosQueDieronLike() {
        return usuariosQueDieronLike;
    }
}
