package com.osc.ociosingluten.controlador.DTO;

public class SeguirDTO {

    private String usernameQueSigueA;
    private String usernameAQuienSigue;

    public SeguirDTO(String usernameQueSigueA, String usernameAQuienSigue) {
        this.usernameQueSigueA = usernameQueSigueA;
        this.usernameAQuienSigue = usernameAQuienSigue;
    }

    public String getUsernameQueSigueA() {
        return usernameQueSigueA;
    }

    public String getUsernameAQuienSigue() {
        return usernameAQuienSigue;
    }
}
