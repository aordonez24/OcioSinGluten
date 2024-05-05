package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Imagen;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public record EstablecimientoDTO(int idEstablecimiento, String nombre,
                                 int telefono, String localidad,
                                 String provincia, String calle, int codPostal,
                                 String pais, int numLikes, List<String> imagenesBase64) {
    public EstablecimientoDTO(Establecimiento est){
        this(est.getIdEstablecimiento(), est.getNombre(), est.getTelefono(), est.getLocalidad(), est.getProvincia(), est.getCalle(),
                est.getCodPostal(), est.getPais(), est.getNumLikes(), convertirImagenesBytes(est.getImagenes()));
    }

    public int idEstablecimiento() {
        return idEstablecimiento;
    }

    public String nombre() {
        return nombre;
    }

    public int telefono() {
        return telefono;
    }

    public String localidad() {
        return localidad;
    }

    public String provincia() {
        return provincia;
    }

    public String calle() {
        return calle;
    }

    public int codPostal() {
        return codPostal;
    }

    public String pais() {
        return pais;
    }

    public int numLikes() {
        return numLikes;
    }

    private static List<String> convertirImagenesBytes(List<Imagen> imagenes) {
        return imagenes.stream()
                .map(Imagen::getImagen)
                .map(EstablecimientoDTO::convertirBytesAStringBase64)
                .collect(Collectors.toList());
    }

    public static String convertirBytesAStringBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
