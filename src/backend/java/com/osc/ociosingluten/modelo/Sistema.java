package com.osc.ociosingluten.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.herramientas.MensajePredefinido;
import com.osc.ociosingluten.herramientas.Rol;


public class Sistema {
    private String nombre;
    private String email;
    private List<Usuario> usuariosRegistrados;
    private List<Establecimiento> establecimientosRegistrados;
    private List<Actividad> actividadesRealizadas;
    private List<Comentario> comentariosRealizados;

    public Sistema() {
        this.nombre = "OcioSinGluten";
        this.email = "aor00039@red.ujaen.es";
        this.usuariosRegistrados = new ArrayList<>();
        this.establecimientosRegistrados = new ArrayList<>();
        this.actividadesRealizadas = new ArrayList<>();
        this.comentariosRealizados = new ArrayList<>();
    }



    /*public boolean marcarFavoritoEstablecimiento(Establecimiento est, Usuario usuario) throws EstablecimientoNoExistenteException {
        if(usuario.isSesionIniciada() && existeEstablecimiento(est)) {
            usuario.anadirEstablecimientoFavorito(est);
            return true;
        }
        return false;
    }

    public boolean darLikeEstablecimiento(Establecimiento est, Usuario usu) throws EstablecimientoNoExistenteException {
        if(usu.isSesionIniciada() && existeEstablecimiento(est)){
            est.setNumLikes(est.getNumLikes()+1);
            Actividad actividad = new Actividad(usu, est, MensajePredefinido.HA_DADO_LIKE);
            crearActividad(actividad, usu);
            return true;
        }
        return false;
    }

    public void seguirUsuario(Usuario usu, Usuario usuASeguir){
        if(usu.isSesionIniciada()){
            if(existeUsuario(usuASeguir)) {
                usu.seguirUsuario(usuASeguir);
            }
        }
    }

    public boolean gestionUsuario(Usuario usu, int modo, Usuario gestionado, String nombre, String apellidos, String password, LocalDate fnac){
        if(modo == 1){ //Eliminamos usuario, solo lo puede hacer el admin
            if(usu.isSesionIniciada() && usu.getRol() == Rol.ADMIN){
                if(existeUsuario(gestionado)){
                    usuariosRegistrados.remove(gestionado);
                    return true;
                }
            }
        }else if(modo == 2){
            //Modificamos datos del usuario
            if(usu.isSesionIniciada()){
                if(!usu.getNombre().equals(nombre)){
                    usu.setNombre(nombre);
                }
                if(!usu.getApellidos().equals(apellidos)){
                    usu.setApellidos(apellidos);
                }
                if(!usu.getPassword().equals(email)){
                    usu.setPassword(password);
                }
                if(usu.getFechaNacimiento() != fnac){
                    usu.setFechaNacimiento(fnac);
                }
                return true;
            }
        } else if (modo == 3) {
            //El usuario quiere dar de baja su cuenta
            if(usu.isSesionIniciada()){
                usuariosRegistrados.remove(usu);
                return true;
            }
        }
        return false;
    }

    public boolean gestionActividades(Actividad cont, Usuario usuario){
        if(usuario.isSesionIniciada() && usuario.getRol() == Rol.ADMIN){
            //Eliminar actividad
            usuario.eliminarActividad(cont);
            actividadesRealizadas.remove(cont);
            return true;
        }
        return false;
    }*/
}