package com.osc.ociosingluten.controlador.DTO;

import com.osc.ociosingluten.modelo.Establecimiento;

import java.util.List;

public record EstablecimientoDTO(int idEstablecimiento, String nombre,
                                 int telefono, String localidad,
                                 String provincia, String calle, int codPostal,
                                 String pais, int numLikes, List<byte[]> imagenes) {
    public EstablecimientoDTO(Establecimiento est){
        this(est.getIdEstablecimiento(), est.getNombre(), est.getTelefono(), est.getLocalidad(), est.getProvincia(), est.getCalle(),
                est.getCodPostal(), est.getPais(), est.getNumLikes(), est.getImagenes());
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


    @Override
    public List<byte[]> imagenes() {
        return imagenes;
    }
}
