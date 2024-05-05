package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Queja;

import java.time.LocalDate;

public record QuejaDTO(int id, String nombre, String email, String mensaje) {

    @Override
    public int id() {
        return id;
    }

    @Override
    public String nombre() {
        return nombre;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public String mensaje() {
        return mensaje;
    }

    public QuejaDTO(Queja q){
        this(q.getIdQueja(), q.getNombreUsuario(), q.getEmail(), q.getMensaje());
    }
}