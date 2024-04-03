package herramientas;

import herramientas.ExpresionesRegulares;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public class Direccion {
    private String localidad;
    private String provincia;
    private String calle;
    private int codPostal;
    private String pais;

    public Direccion(String localidad, String provincia, String calle, int codPostal, String pais) {
        this.localidad = localidad;
        this.provincia = provincia;
        this.calle = calle;
        this.codPostal = codPostal;
        this.pais = pais;
    }

    public Direccion() {

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

    public int getCodPostal() {
        return codPostal;
    }

    public void setCodPostal(int codPostal) {
        this.codPostal = codPostal;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }
}
