package modelo;

import herramientas.Direccion;
import herramientas.ExpresionesRegulares;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import static constantes.Constantes.mediaValoracion;

@Entity
public class Establecimiento {
    @Id
    private int idEstablecimiento;

    @NotNull
    @Size(min = 0, max = 30)
    private String nombre;

    @Pattern(regexp= ExpresionesRegulares.TLF)
    private int telefono;

    @Embedded
    private Direccion direccion;

    @NotNull
    private int numLikes;

    @OneToMany
    private ArrayList<Comentario> comentarios;

    @ManyToMany
    private ArrayList<Usuario> visitantes;

    public Establecimiento(String nombre, int telefono, Direccion direccion) {
        this.idEstablecimiento = generarIdUnico(direccion.getLocalidad(), direccion.getProvincia(), direccion.getCodPostal());
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.numLikes = 0;
        this.comentarios = new ArrayList<>();
        this.visitantes = new ArrayList<>();
    }

    public Establecimiento(String nombre, int telefono, Direccion direccion, int numLikes, ArrayList<Comentario> comentarios) {
        this.idEstablecimiento = generarIdUnico(direccion.getLocalidad(), direccion.getProvincia(), direccion.getCodPostal());
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.numLikes = numLikes;
        this.comentarios = comentarios;
    }

    public Establecimiento() {

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTelefono() {
        return telefono;
    }

    public void setTelefono(int telefono) {
        this.telefono = telefono;
    }

    public int getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(int numLikes) {
        this.numLikes = numLikes;
    }

    public ArrayList<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(ArrayList<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public double obtenerValoracion(Establecimiento est){
        return (double) est.getNumLikes()/mediaValoracion;
    }

    public void anadirComentario(Comentario comentario){comentarios.add(comentario);}

    public static int generarIdUnico(String str1, String str2, int num) {
        try {
            String input = str1 + str2 + num;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convertir los primeros 4 bytes del hash a un entero para usar como ID
            int result = 0;
            for (int i = 0; i < 4; i++) {
                result += (hash[i] & 0xFF) << (8 * (3 - i));
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // Manejo de la excepción en caso de que el algoritmo no esté disponible
            // Podrías devolver un valor predeterminado o lanzar una excepción personalizada
            return 0;
        }
    }

    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(int idEstablecimiento) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public ArrayList<Usuario> getVisitantes() {
        return visitantes;
    }

    public void setVisitantes(ArrayList<Usuario> visitantes) {
        this.visitantes = visitantes;
    }

    public void anadirVisitante(Usuario usu){
        this.visitantes.add(usu);
    }

}
