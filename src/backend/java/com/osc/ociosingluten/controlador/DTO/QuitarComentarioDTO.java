package com.osc.ociosingluten.controlador.DTO;

public class QuitarComentarioDTO {

    private String username;
    private int idEstablecimiento;


    public QuitarComentarioDTO(String username, int est) {
        this.username = username;
        this.idEstablecimiento = est;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getEst() {
        return idEstablecimiento;
    }

    public void setEst(int est) {
        this.idEstablecimiento = est;
    }
}
