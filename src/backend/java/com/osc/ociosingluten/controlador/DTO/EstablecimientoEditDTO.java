package com.osc.ociosingluten.controlador.DTO;

public class EstablecimientoEditDTO {
    private String nombre;
    private String telefono;
    private String localidad;
    private String provincia;
    private String calle;
    private String codPostal;
    private String pais;

    // Constructores, getters y setters

    public EstablecimientoEditDTO() {
    }

    public EstablecimientoEditDTO(String nombre, String telefono, String localidad, String provincia, String calle, String codPostal, String pais) {
        this.nombre = nombre;
        this.telefono = telefono;
        this.localidad = localidad;
        this.provincia = provincia;
        this.calle = calle;
        this.codPostal = codPostal;
        this.pais = pais;
    }

    // Getters y setters

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getCodPostal() {
        return codPostal;
    }

    public String getPais() {
        return pais;
    }
}

