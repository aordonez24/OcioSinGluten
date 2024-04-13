package com.osc.ociosingluten.modelo;

import com.osc.ociosingluten.excepciones.ComentarioNoExiste;
import com.osc.ociosingluten.herramientas.ExpresionesRegulares;
import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static com.osc.ociosingluten.constantes.Constantes.mediaValoracion;

@Entity
@Table (name = "establecimientos")
public class Establecimiento {

    @Id
    private int idEstablecimiento;

    @NotNull
    @Size(min = 0, max = 30)
    private String nombre;

    @NotNull
    @Digits(integer = 9, fraction = 0)
    private int telefono;

    @NotNull
    private String localidad;

    @NotNull
    private String provincia;

    @NotNull
    private String calle;

    @NotNull
    private int codPostal;

    @NotNull
    private String pais;

    @NotNull
    private int numLikes;

    @OneToMany
    private List<Comentario> comentarios;

    @ManyToMany
    private List<Usuario> visitantes;

    @NotNull
    private boolean archivada; //Si un establecimiento esta archivado, no estará eliminado de la base de datos pero tampoco aparecerá a los usuarios

    public Establecimiento(String nombre, int telefono, String localidad, String provincia, String calle, int codPostal, String pais) {
        this.idEstablecimiento = generarIdUnico(localidad, provincia, codPostal);
        this.nombre = nombre;
        this.telefono = telefono;
        this.localidad = localidad;
        this.provincia = provincia;
        this.calle = calle;
        this.codPostal = codPostal;
        this.pais = pais;
        this.numLikes = 0;
        this.comentarios = new ArrayList<>();
        this.visitantes = new ArrayList<>();
        this.archivada = false;
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

    public void sumarLike() {
        this.numLikes += 1;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
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

            int result = 0;
            for (int i = 0; i < 4; i++) {
                result += (hash[i] & 0xFF) << (8 * (3 - i));
            }
            // Asegurarse de que el resultado sea positivo
            return Math.abs(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public int getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(int idEstablecimiento) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public List<Usuario> getVisitantes() {
        return visitantes;
    }

    public void setVisitantes(ArrayList<Usuario> visitantes) {
        this.visitantes = visitantes;
    }

    public void anadirVisitante(Usuario usu){
        this.visitantes.add(usu);
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

    public void setVisitantes(List<Usuario> visitantes) {
        this.visitantes = visitantes;
    }

    public void eliminarComentario(Comentario com) throws ComentarioNoExiste {
        if(comentarios.contains(com)){
            comentarios.remove(com);
        }else{
            throw new ComentarioNoExiste("El comentario no existe.");
        }
    }

    public boolean isArchivada() {
        return archivada;
    }

    public void setArchivada(boolean archivada) {
        this.archivada = archivada;
    }
}
