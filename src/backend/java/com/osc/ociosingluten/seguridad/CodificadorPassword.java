package com.osc.ociosingluten.seguridad;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CodificadorPassword {

    static BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private CodificadorPassword(){

    }

    public static String codificar(String cadena){
        return encoder.encode(cadena);
    }

    public static boolean igual(String password, String passwordCodificado){
        return encoder.matches(password, passwordCodificado);
    }
}
