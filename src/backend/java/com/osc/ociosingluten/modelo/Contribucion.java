package com.osc.ociosingluten.modelo;

import java.time.LocalDate;

enum MensajePredefinido {
    HA_VISITADO, HA_PUBLICADO
};
public class Contribucion {
    private Usuario autor;
    private Establecimiento establecimiento;
    private LocalDate fechaContribucion;
    private MensajePredefinido mensajePredefinido;

    public Contribucion(Usuario autor, Establecimiento establecimiento, LocalDate fechaContribucion, MensajePredefinido mensajePredefinido) {
        this.autor = autor;
        this.establecimiento = establecimiento;
        this.fechaContribucion = fechaContribucion;
        this.mensajePredefinido = mensajePredefinido;
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
