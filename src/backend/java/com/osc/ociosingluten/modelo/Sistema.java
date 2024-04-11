package com.osc.ociosingluten.modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.osc.ociosingluten.constantes.Constantes.*;

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


    private void crearActividad(Actividad act, Usuario usu){
        if(!actividadesRealizadas.contains(act)) {
            actividadesRealizadas.add(act);
            usu.anadirActividad(act);
        }
    }


    public Establecimiento buscarEstablecimiento(Establecimiento est) throws EstablecimientoNoExistenteException {
        if(establecimientosRegistrados.contains(est)) {
            return est;
        } else {
            throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
        }
    }


    public boolean anadirComentario(String texto, Establecimiento establecimiento, Usuario usu){
        //Usu es el usuario que realiza el comentario
        if(usu.isSesionIniciada() && existeUsuario(usu)) {
            if (texto.length() > 0 && texto.length() <= 140) {
                Comentario com = new Comentario(texto, usu);
                establecimiento.anadirComentario(com);
                usu.enviarComentario(com);
                this.comentariosRealizados.add(com);
                Actividad cont = new Actividad(usu, establecimiento, MensajePredefinido.HA_COMENTADO);
                crearActividad(cont, usu);
                return true;
            }
        }
        return false;
    }

    public boolean eliminarComentario(Comentario c, Usuario usu){
        if(c.getAutor() == usu && usu.isSesionIniciada() && existeUsuario(usu)){
            usu.eliminarComentario(c);
            for(int i=0; i<comentariosRealizados.size(); i++){
                if(c == comentariosRealizados.get(i)) {
                    comentariosRealizados.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean editarComentario(Comentario c, String textoNuevo, Usuario usu){
        if(c != null) {
            if (c.getAutor() == usu && usu.isSesionIniciada() && existeUsuario(usu)) {
                if (c.getMensaje().equals(textoNuevo))
                    return false;
                else {
                    c.setMensaje(textoNuevo);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean eliminarEstablecimiento(Establecimiento est, Usuario usu) throws EstablecimientoNoExistenteException {
        if (usu != null && usu.getRol() == Rol.ADMIN && usu.isSesionIniciada() && existeUsuario(usu)) {
            for (int i = 0; i < establecimientosRegistrados.size(); i++) {
                if (est == establecimientosRegistrados.get(i)) {
                    establecimientosRegistrados.remove(i);
                    return true;
                }
            }
            throw new EstablecimientoNoExistenteException("El establecimiento no existe en la lista.");
        }
        return false;
    }


    public boolean existeUsuario(Usuario usu){
        if(usuariosRegistrados.contains(usu)){
            return true;
        }
        return false;
    }

    private boolean existeEstablecimiento(Establecimiento est) throws EstablecimientoNoExistenteException {
        if(establecimientosRegistrados.contains(est))
            return true;
        else
            throw new EstablecimientoNoExistenteException("El establecimiento no existe en la lista.");
    }

    public boolean visitarEstablecimiento(Establecimiento est, Usuario usuario) throws EstablecimientoNoExistenteException {
        if(usuario.isSesionIniciada() && existeEstablecimiento(est)) {
            Actividad actividad = new Actividad(usuario, est, MensajePredefinido.HA_VISITADO);
            crearActividad(actividad, usuario);
            usuario.anadirEstablecimientoVisitado(est, actividad);
            return true;
        }
        return false;
    }
    public boolean marcarFavoritoEstablecimiento(Establecimiento est, Usuario usuario) throws EstablecimientoNoExistenteException {
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
    }

    public void anadirUsuarioPrueba(Usuario usu){
        if(!usuariosRegistrados.contains(usu)){
            usuariosRegistrados.add(usu);
        }
    }
    public void anadirEstablecimientoPrueba(Establecimiento usu){
        if(!establecimientosRegistrados.contains(usu)){
            establecimientosRegistrados.add(usu);
        }
    }

    public void anadirActividadPrueba(Actividad usu){
        if(!actividadesRealizadas.contains(usu)){
            actividadesRealizadas.add(usu);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public List<Usuario> getUsuariosRegistrados() {
        return usuariosRegistrados;
    }

    public List<Establecimiento> getEstablecimientosRegistrados() {
        return establecimientosRegistrados;
    }

    public List<Actividad> getActividadesRealizadas() {
        return actividadesRealizadas;
    }

    public List<Comentario> getComentariosRealizados() {
        return comentariosRealizados;
    }
}