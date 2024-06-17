package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Comentario;

import java.time.LocalDate;
import java.util.List;

public record ComentarioDTO(int id, String nombreAutor,
                            LocalDate fecha, String mensaje) {

    public ComentarioDTO(Comentario com){
        this(com.getId(), com.getAutor().getUsername(), com.getFecha(), com.getMensaje());
    }

    public int id() {
        return id;
    }

    public String nombreAutor() {
        return nombreAutor;
    }

    public LocalDate fecha() {
        return fecha;
    }

    public String mensaje() {
        return mensaje;
    }


}
