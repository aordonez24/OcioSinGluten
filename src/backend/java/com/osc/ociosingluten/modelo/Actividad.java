package com.osc.ociosingluten.modelo;

import com.osc.ociosingluten.herramientas.MensajePredefinido;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table (name = "actividades")
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // Generar ID automáticamente
    private int id;

    @ManyToOne
    @JoinColumn(name = "dni") // Nombre de la columna en la tabla de Actividad que hace referencia al usuario
    private Usuario autor;


    @ManyToOne
    @JoinColumn(name = "idEstablecimiento") // Nombre de la columna en la tabla de Actividad que hace referencia al establecimiento
    private Establecimiento establecimiento;

    @NotNull
    private LocalDate fechaContribucion;

    @Enumerated(EnumType.STRING)
    @NotNull
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

    public Actividad() {

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

    public int getId() {
        return id;
    }
}
