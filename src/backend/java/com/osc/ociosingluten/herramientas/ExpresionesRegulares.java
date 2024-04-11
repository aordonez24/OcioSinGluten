package com.osc.ociosingluten.herramientas;

public class ExpresionesRegulares {
    private ExpresionesRegulares() {
    }

    public static final String CONTRASENA = "^(?=.*[A-Z])(?=.*\\d).{8,}$";
    public static final String DNI = "\\d{8}[A-HJ-NP-TV-Z]";

}
