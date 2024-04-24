package com.osc.ociosingluten.herramientas;

public class LoginMessage {
    String mensaje;
    boolean status;

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public LoginMessage(String mensaje, boolean status) {
        this.mensaje = mensaje;
        this.status = status;
    }
}
