package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Consulta;

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

    public QuejaDTO(Consulta q){
        this(q.getIdQueja(), q.getNombreUsuario(), q.getEmail(), q.getMensaje());
    }
}