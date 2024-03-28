package modelo;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.osc.ociosingluten.constantes.Constantes.maxTelefono;

public class Sistema {
    private String nombre;
    private String email;
    private ArrayList<Usuario> usuariosRegistrados;
    private ArrayList<Establecimiento> establecimientosRegistrados;
    private ArrayList<Contribucion> contribucionesRealizadas;
    private ArrayList<Comentario> comentariosRealizados;


    private String hashContrasena(String password){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for(byte b : hashBytes){
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean iniciarSesion(String email, String password){
        for(int i=0; i< usuariosRegistrados.size(); i++){
            if(usuariosRegistrados.get(i).getEmail().equals(email)){
                //Existe ese email
                if(password.equals(hashContrasena(password))){
                    usuariosRegistrados.get(i).setSesionIniciada(true);
                    usuariosRegistrados.get(i).setSesionCerrada(false);
                    return true;
                }
                else
                    return false;
            }else{
                return false;
            }
        }
        return false;
    }

    public boolean cerrarSesion(Usuario usuario){
        if(existeUsuario(usuario)){
            usuario.setSesionCerrada(true);
            usuario.setSesionIniciada(false);
            return true;
        }
        return false;
    }

    public boolean registro(String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, Image fotoPerfil, String email, String password){
        //Comprueba si existe ese usuario
        for(int i=0; i< usuariosRegistrados.size(); i++){
            if(usuariosRegistrados.get(i).getEmail().equals(email) || usuariosRegistrados.get(i).getUsername().equals(username))
                return false;
            else
                usuariosRegistrados.add(new Usuario(username, nombre, apellidos, fechaNacimiento, telefono, fotoPerfil, email, password));
        }
        return false;
    }

    public Usuario buscarUsuario(Usuario usuario){
        if(usuario.isSesionIniciada()) {
            if (existeUsuario(usuario))
                return usuario;
        }
        return null;
    }

    public boolean anadirComentario(String texto, Establecimiento establecimiento, Usuario usu){
        //Usu es el usuario que realiza el comentario
        if(usu.isSesionIniciada()) {
            if (texto.length() > 0 && texto.length() <= 140) {
                Comentario com = new Comentario(texto, usu);
                establecimiento.anadirComentario(com);
                usu.enviarComentario(com);
                this.comentariosRealizados.add(com);
                Contribucion cont = new Contribucion(usu, establecimiento, MensajePredefinido.HA_COMENTADO);
                this.contribucionesRealizadas.add(cont);
                return true;
            }
        }
        return false;
    }

    public boolean eliminarComentario(Comentario c, Usuario usu){
        if(c.getAutor() == usu && usu.isSesionIniciada()){
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
        if(c.getAutor() == usu && usu.isSesionIniciada()) {
            if(c.getMensaje().equals(textoNuevo))
                return false;
            else{
                c.setMensaje(textoNuevo);
                return true;
            }
        }
        return false;
    }

    public boolean anadirEstablecimiento(String nombre, String direccion, int telefono, Usuario usu){
        String numeroComoCadena = String.valueOf(telefono);
        if(nombre.length() > 0 && nombre.length() <= 20 && numeroComoCadena.length() == maxTelefono && direccion.length() > 0 && direccion.length() <= 100){
            Establecimiento est = new Establecimiento(nombre, telefono, direccion);
            establecimientosRegistrados.add(est);
            Contribucion cont = new Contribucion(usu, est, MensajePredefinido.HA_PUBLICADO);
            this.contribucionesRealizadas.add(cont);
            return true;
        }
        return false;
    }

    public boolean eliminarEstablecimiento(Establecimiento est, Usuario usu){
        if(usu.getRol() == Rol.ADMIN && usu.isSesionIniciada()){
            for(int i=0; i<establecimientosRegistrados.size(); i++){
                if(est == establecimientosRegistrados.get(i)){
                    establecimientosRegistrados.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean editarEstablecimiento(Establecimiento est, Usuario usu, String nombre, String direccion, int telefono) {
        if (usu.getRol() == Rol.ADMIN && usu.isSesionIniciada()) {
            for (int i = 0; i < establecimientosRegistrados.size(); i++) {
                if (est == establecimientosRegistrados.get(i)) {
                    String numeroComoCadena = String.valueOf(telefono);
                    if (nombre.length() > 0 && nombre.length() <= 20 && numeroComoCadena.length() == maxTelefono && direccion.length() > 0 && direccion.length() <= 100)
                    {
                        if(nombre != est.getNombre())
                            est.setNombre(nombre);
                        if(direccion != est.getDireccion())
                            est.setDireccion(direccion);
                        if(telefono != est.getTelefono())
                            est.setTelefono(telefono);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean existeUsuario(Usuario usu){
        for(Usuario usua : usuariosRegistrados){
            if(usuariosRegistrados.contains(usu))
                return true; //Usuario existe
        }
        return false;
    }

    public void visitarEstablecimiento(Establecimiento est, Usuario usuario){
        if(usuario.isSesionIniciada()) {
            Contribucion contribucion = new Contribucion(usuario, est, MensajePredefinido.HA_VISITADO);
            usuario.anadirEstablecimientoVisitado(est);
        }
    }
    public void marcarFavoritoEstablecimiento(Establecimiento est, Usuario usuario){
        if(usuario.isSesionIniciada()) {
            usuario.anadirEstablecimientoFavorito(est);
        }
    }

    public void seguirUsuario(Usuario usu, Usuario usuASeguir){
        if(usu.isSesionIniciada()){
            if(existeUsuario(usuASeguir)) {
                usu.seguirUsuario(usuASeguir);
            }
        }
    }


}