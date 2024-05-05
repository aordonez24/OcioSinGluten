package com.osc.ociosingluten.modelo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "imagenes_establecimiento")
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE) // Generar ID automáticamente
    private int idImagen;

    @Lob
    @Column(name = "imagen", columnDefinition = "LONGBLOB")
    private byte[] imagen;

    @ManyToOne
    @JoinColumn(name = "id_establecimiento")
    @JsonIgnore // Ignorar esta propiedad al serializar a JSON
    private Establecimiento establecimiento;

    public Imagen(byte[] imagen, Establecimiento establecimiento) {
        this.imagen = imagen;
        this.establecimiento = establecimiento;
    }

    public Imagen() {

    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }
}
