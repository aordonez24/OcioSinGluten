package com.osc.ociosingluten.modelo;

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

    @OneToMany(mappedBy = "comentarioPadre", fetch = FetchType.EAGER)
    private List<Comentario> comentarios;

    /*
    La idea de tener un atributo comentarioPadre en la clase Comentario es permitir la creación de una estructura de comentarios anidados o comentarios secundarios. Esto es útil en escenarios donde se permite a los usuarios responder a comentarios existentes.

    Por ejemplo, supongamos que un usuario deja un comentario inicial en una publicación, y otros usuarios pueden responder a ese comentario original. Cada respuesta sería un comentario secundario y estaría asociado al comentario padre al que está respondiendo.

    Al tener un atributo comentarioPadre, puedes establecer fácilmente las relaciones entre comentarios principales y sus respuestas. Esto facilita la navegación a través de la estructura de comentarios y permite mostrar los comentarios de manera jerárquica en la interfaz de usuario, si es necesario.
     */
    @ManyToOne
    private Comentario comentarioPadre;

    public Comentario(String mensaje, Usuario autor) {
        this.mensaje = mensaje;
        this.autor = autor;
        this.numLikes = 0;
        this.fecha = LocalDate.now();
        this.comentarios = new ArrayList<>();
    }

    public Comentario(String mensaje, Usuario autor, int numLikes, LocalDate fecha, ArrayList<Comentario> comentarios) {
        this.mensaje = mensaje;
        this.autor = autor;
        this.numLikes = numLikes;
        this.fecha = fecha;
        this.comentarios = comentarios;
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

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void anadirComentario(Comentario comentario){
        comentarios.add(comentario);
    }
    public void quitarComentario(Comentario comentario){
        comentarios.remove(comentario);
    }

    public void setComentarioPadre(Comentario comentarioPadre) {
        this.comentarioPadre = comentarioPadre;
    }
}
