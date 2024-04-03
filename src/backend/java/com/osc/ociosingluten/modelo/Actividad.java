package modelo;

import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
public class Actividad {
    private Usuario autor;
    private Establecimiento establecimiento;
    private LocalDate fechaContribucion;
    private MensajePredefinido mensajePredefinido;

    public Actividad(Usuario autor, Establecimiento establecimiento, LocalDate fechaContribucion, MensajePredefinido mensajePredefinido) {
        this.autor = autor;
        this.establecimiento = establecimiento;
        this.fechaContribucion = fechaContribucion;
        this.mensajePredefinido = mensajePredefinido;
    }

    public Actividad(Usuario autor, Establecimiento establecimiento, MensajePredefinido mensajePredefinido) {
        this.autor = autor;
        this.establecimiento = establecimiento;
        this.mensajePredefinido = mensajePredefinido;
        this.fechaContribucion = LocalDate.now();
    }

    public Usuario getAutor() {
        return autor;
    }

    public void setAutor(Usuario autor) {
        this.autor = autor;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public LocalDate getFechaContribucion() {
        return fechaContribucion;
    }

    public void setFechaContribucion(LocalDate fechaContribucion) {
        this.fechaContribucion = fechaContribucion;
    }

    public MensajePredefinido getMensajePredefinido() {
        return mensajePredefinido;
    }

    public void setMensajePredefinido(MensajePredefinido mensajePredefinido) {
        this.mensajePredefinido = mensajePredefinido;
    }
}
