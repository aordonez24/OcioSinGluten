package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Actividad;

import java.time.LocalDate;

public record ActividadDTO(int id, String usernameAutor, String nombreEstablecimiento, LocalDate fechaContribucion, String mensajePredefinido) {

    public int id() {
        return id;
    }

    public String usernameAutor() {
        return usernameAutor;
    }

    public String nombreEstablecimiento() {
        return nombreEstablecimiento;
    }

    public LocalDate fechaContribucion() {
        return fechaContribucion;
    }

    public String mensajePredefinido() {
        return mensajePredefinido;
    }

    public ActividadDTO(Actividad act){
        this(act.getId(), act.getAutor().getUsername(), act.getEstablecimiento().getNombre(), act.getFechaContribucion(), act.getMensajePredefinido().toString());
    }
}
