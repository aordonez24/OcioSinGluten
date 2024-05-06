package com.osc.ociosingluten.controlador.DTO;

public class NuevoComentarioDTO {
    private String username;
    private String mensaje;

    public NuevoComentarioDTO(String username, String mensaje) {
        this.username = username;
        this.mensaje = mensaje;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
