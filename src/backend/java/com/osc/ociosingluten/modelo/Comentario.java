package modelo;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.ArrayList;

public class Comentario {
    private String mensaje;
    private Usuario autor;
    private int numLikes;
    private LocalDate fecha;
    private ArrayList<Comentario> comentarios;

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

    public ArrayList<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(ArrayList<Comentario> comentarios) {
        this.comentarios = comentarios;
    }
}
