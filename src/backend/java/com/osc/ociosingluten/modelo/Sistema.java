package modelo;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;

import static constantes.Constantes.*;


public class Sistema {
    private String nombre;
    private String email;
    private ArrayList<Usuario> usuariosRegistrados;
    private ArrayList<Establecimiento> establecimientosRegistrados;
    private ArrayList<Actividad> actividadesRealizadas;
    private ArrayList<Comentario> comentariosRealizados;

    public Sistema() {
        this.nombre = "OcioSinGluten";
        this.email = "aor00039@red.ujaen.es";
        this.usuariosRegistrados = new ArrayList<>();
        this.establecimientosRegistrados = new ArrayList<>();
        this.actividadesRealizadas = new ArrayList<>();
        this.comentariosRealizados = new ArrayList<>();
    }

    private String hashContrasena(String password) {
        try {
            // Generar salt aleatorio
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            byte[] passwordBytes = password.getBytes();
            byte[] saltedPassword = new byte[passwordBytes.length + salt.length];
            System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.length);
            System.arraycopy(salt, 0, saltedPassword, passwordBytes.length, salt.length);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(saltedPassword);
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
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
            if(usuariosRegistrados.get(i).getEmail().equals(email) && usuariosRegistrados.get(i).getPassword().equals(password)){
                //Existe ese email
                    usuariosRegistrados.get(i).setSesionIniciada(true);
                    usuariosRegistrados.get(i).setSesionCerrada(false);
                    return true;
            }else{
                usuariosRegistrados.get(i).setSesionIniciada(false);
                usuariosRegistrados.get(i).setSesionCerrada(true);
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

    private String generarContrasenaAleatoria() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int longitud = MaxlargoContrasena;

        StringBuilder contrasena = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < longitud; i++) {
            int indice = random.nextInt(caracteres.length());
            contrasena.append(caracteres.charAt(indice));
        }
        return contrasena.toString();
    }

    private void crearActividad(Actividad act, Usuario usu){
        if(!actividadesRealizadas.contains(act)) {
            actividadesRealizadas.add(act);
            usu.anadirActividad(act);
        }
    }

    public void olvidarContraseña(Usuario usu){
        //usu es el usuario al que se le olvida la contraseña
        if(usu.isSesionIniciada()){
            usu.setPassword(generarContrasenaAleatoria());
        }
    }

    public boolean registro(String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, Image fotoPerfil, String email, String password){
        //Comprueba si existe ese usuario
        for(int i=0; i< usuariosRegistrados.size(); i++){
            if(usuariosRegistrados.get(i).getEmail().equals(email) || usuariosRegistrados.get(i).getUsername().equals(username)) //Comprueba si ese email o username esta registrado
                return false;
            else {
                usuariosRegistrados.add(new Usuario(username, nombre, apellidos, fechaNacimiento, telefono, fotoPerfil, email, password));
                return true;
            }
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

    public Establecimiento buscarEstablecimiento(Establecimiento est){
        if(establecimientosRegistrados.contains(est))
            return est;
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
                Actividad cont = new Actividad(usu, establecimiento, MensajePredefinido.HA_COMENTADO);
                crearActividad(cont, usu);
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
            Actividad cont = new Actividad(usu, est, MensajePredefinido.HA_PUBLICADO);
            crearActividad(cont, usu);
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
            Actividad actividad = new Actividad(usuario, est, MensajePredefinido.HA_VISITADO);
            crearActividad(actividad, usuario);
            usuario.anadirEstablecimientoVisitado(est, actividad);
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

    public void gestionUsuario(Usuario usu, int modo, Usuario gestionado, String nombre, String apellidos, String password, LocalDate fnac){
        if(modo == 1){ //Eliminamos usuario, solo lo puede hacer el admin
            if(usu.isSesionIniciada() && usu.getRol() == Rol.ADMIN){
                if(existeUsuario(gestionado)){
                    usuariosRegistrados.remove(gestionado);
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
            }
        } else if (modo == 3) {
            //El usuario quiere dar de baja su cuenta
            if(usu.isSesionIniciada()){
                usuariosRegistrados.remove(usu);
            }
        }
    }

    public boolean gestionContribuciones(Actividad cont, Usuario usuario){
        if(usuario.isSesionIniciada() && usuario.getRol() == Rol.ADMIN){
            //Eliminar contribucion
            usuario.eliminarActividad(cont);
            actividadesRealizadas.remove(cont);
            return true;
        }
        return false;
    }

    public void anadirUsuario(Usuario usu){
        if(!usuariosRegistrados.contains(usu)){
            usuariosRegistrados.add(usu);
        }
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<Usuario> getUsuariosRegistrados() {
        return usuariosRegistrados;
    }

    public ArrayList<Establecimiento> getEstablecimientosRegistrados() {
        return establecimientosRegistrados;
    }

    public ArrayList<Actividad> getActividadesRealizadas() {
        return actividadesRealizadas;
    }

    public ArrayList<Comentario> getComentariosRealizados() {
        return comentariosRealizados;
    }
}