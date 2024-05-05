package com.osc.ociosingluten.servicio;

import com.osc.ociosingluten.controlador.DTO.LoginDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.herramientas.LoginMessage;
import com.osc.ociosingluten.herramientas.MensajePredefinido;
import com.osc.ociosingluten.herramientas.Rol;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.swing.text.html.Option;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.osc.ociosingluten.constantes.Constantes.MaxlargoContrasena;

@Service
public class ServicioOcioSinGluten {

    @Autowired
    UsuarioRepository repoUsuario;

    @Autowired
    ActividadRepository repoAct;

    @Autowired
    EstablecimientoRepository repoEst;

    @Autowired
    ComentarioRepository repoCom;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;


    public ServicioOcioSinGluten() {
    }

    /**
     * Funcion utilizada para el registro del usuario, el registro se simula añadiendo un usuario a la base de datos
     * @param usu Usuario a registrar
     * @return True si se ha registrado correctamente
     * @throws UsuarioExisteException si ese usuario existe en la base de datos
     */
    public boolean registroUsuario(@NotNull @Valid Usuario usu) throws UsuarioExisteException {
        Optional<Usuario> usuarioPorDni = repoUsuario.findByDni(usu.getDni());
        Optional<Usuario> usuarioPorEmail = repoUsuario.findByEmail(usu.getEmail());
        Optional<Usuario> usuarioPorUsername = repoUsuario.findByUsername(usu.getUsername());

        if (usuarioPorDni.isPresent()) {
            throw new UsuarioExisteException("El usuario con este DNI ya existe.");
        } else if (usuarioPorEmail.isPresent()) {
            throw new UsuarioExisteException("El usuario con este email ya existe.");
        } else if (usuarioPorUsername.isPresent()) {
            throw new UsuarioExisteException("El usuario con este nombre de usuario ya existe.");
        } else {
            usu.setPassword(bCryptPasswordEncoder.encode(usu.getPassword()));
            repoUsuario.save(usu);
            return true;
        }
    }


    public Usuario loginUsuario(@NotBlank String email, @NotBlank String password) throws UsuarioNoExisteException, ContrasenaIncorrectaException {
        if(!repoUsuario.findByEmail(email).isPresent()){
            throw new UsuarioNoExisteException("El usuario no existe");
        }
        Optional<Usuario> clienteLogin = repoUsuario.findByEmail(email).filter((usuario -> usuario.claveValida(password)));
        if(clienteLogin.isPresent()) {
            Usuario usu = clienteLogin.get();
            usu.setSesionIniciada(true);
            usu.setSesionCerrada(false);
            repoUsuario.save(usu);
            return usu;
        }else{
            throw new ContrasenaIncorrectaException("La contraseña es incorrecta.");
        }

    }


    public Usuario logoutUsuario(@NotBlank String email, @NotBlank String password) throws UsuarioNoExisteException, SesionNoIniciadaException, ContrasenaIncorrectaException {
        Optional<Usuario> usu = repoUsuario.findByEmail(email).stream().findFirst();

        if(usu.isPresent()){
            Usuario usuario = usu.get();
            if(usuario.isSesionIniciada()) {
                usuario.setSesionCerrada(true);
                usuario.setSesionIniciada(false);
                repoUsuario.actualizarUsuario(usuario);
                return usuario;
            }else{
                throw new SesionNoIniciadaException("No se ha iniciado sesion");
            }
        }else{
            throw new UsuarioNoExisteException("No existe ese email registrado.");
        }
    }

    public byte[] convertirImagenAByte(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        return outputStream.toByteArray();
    }

    public BufferedImage cargarImagenDesdeBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        return ImageIO.read(bis);
    }

    public void olvidarContrasena(Usuario usuario) throws UsuarioNoExisteException, SesionNoIniciadaException {
        Optional<Usuario> usu = repoUsuario.findByDni(usuario.getDni());
        if(usu.isPresent()){
            if(usu.get().isSesionIniciada()) {
                usuario.setPassword(generarContrasenaAleatoria());
                repoUsuario.actualizarUsuario(usuario);
            }else{
                throw new SesionNoIniciadaException("El usuario no ha iniciado sesión");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
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


    public Usuario buscarUsuario(Usuario usuario) throws UsuarioNoExisteException {
        Optional<Usuario> usuarios = repoUsuario.findByUsername(usuario.getUsername());
        if(usuarios.isPresent() && !usuarios.get().isArchivado()) {
            return usuarios.get(); // Por ejemplo, podrías devolver el primer usuario de la lista
        } else {
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public Usuario buscarUsuarioXUsername(String username) throws UsuarioNoExisteException {
        Optional<Usuario> usuarios = repoUsuario.findByUsername(username);
        if(usuarios.isPresent() && !usuarios.get().isArchivado()) {
            return usuarios.get(); // Por ejemplo, podrías devolver el primer usuario de la lista
        } else {
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public List<Establecimiento> buscarEstablecimiento(Establecimiento establecimiento) throws UsuarioNoExisteException {
        List<Establecimiento> establecimientos = repoEst.findByNombre(establecimiento.getNombre());
        if(!establecimientos.isEmpty()) {
            List<Establecimiento> establecimientosDevolver = new ArrayList<>();
            for(Establecimiento est: establecimientos){
                if(!est.isArchivada())
                    establecimientosDevolver.add(est);
            }
            return establecimientosDevolver;
        } else {
            throw new UsuarioNoExisteException("El establecimiento no existe.");
        }

    }

    public Usuario cambiarPermisos(Usuario usu, Usuario aCambiar) throws SesionNoIniciadaException, NoPermisosException, UsuarioNoExisteException {
        Optional<Usuario> usuario = repoUsuario.findByDni(usu.getDni());
        Usuario gestor = usuario.get();
        if(usuario.isPresent()) {
            if (gestor.isSesionIniciada()) {
                if(gestor.getRol() == Rol.ADMIN){
                    if(aCambiar.getRol() == Rol.ADMIN){
                        aCambiar.setRol(Rol.COMUN);
                    }else{
                        aCambiar.setRol(Rol.ADMIN);
                    }
                    return aCambiar;
                }else{
                    throw new NoPermisosException("El usuario no tiene permisos para realizar esta operacion.");
                }
            }else{
                throw new SesionNoIniciadaException("No ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public boolean publicarEstablecimiento(Usuario anadeEst, Establecimiento est) throws SesionNoIniciadaException, EstablecimientoExistenteException, ActividadNoCreada {
        Optional<Establecimiento> establecimiento = repoEst.findByNombreAndCodPostal(est.getNombre(), est.getCodPostal());
        if(establecimiento.isPresent()){
            throw new EstablecimientoExistenteException("El establecimiento ya existe.");
        }else{
            repoEst.save(est);
            Actividad act = new Actividad(anadeEst, est, MensajePredefinido.HA_PUBLICADO);
            crearActividad(act);
            anadeEst.anadirActividad(act);
            repoUsuario.actualizarUsuario(anadeEst);
            return true;
        }
    }

    public boolean eliminarEstablecimiento(Usuario usuGestor, Establecimiento est) throws EstablecimientoNoExistenteException, ActividadNoCreada, SesionNoIniciadaException, NoPermisosException, UsuarioNoExisteException {
        //Esta opción solo la puede hacer un usuario con la sesion iniciada y si es un admin
        Optional<Usuario> usuu = repoUsuario.findByDni(usuGestor.getDni());
        if(usuu.isPresent()){
            if (usuGestor.isSesionIniciada()) {
                if (usuGestor.getRol() == Rol.ADMIN) {
                    Optional<Establecimiento> establecimiento = repoEst.findByIdEstablecimiento(est.getIdEstablecimiento());
                    if (establecimiento.isPresent()) {
                        est.setArchivada(true);
                        return true;
                    } else {
                        throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
                    }
                } else {
                    throw new NoPermisosException("El usuario no tiene permisos para realizar esa acción.");
                }
            }
            throw new SesionNoIniciadaException("El usuario no ha iniciado sesion.");
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public boolean editarEstablecimiento(Usuario usuGestor, Establecimiento est, String nuevoNombre, int nuevoTelefono, String nuevaLocalidad, String nuevaProvincia, String nuevaCalle, int nuevoCodPostal, String nuevoPais) throws SesionNoIniciadaException, NoPermisosException, EstablecimientoNoExistenteException {
        if(usuGestor.isSesionIniciada()) {
            if(usuGestor.getRol() == Rol.ADMIN) {
                Optional<Establecimiento> establecimiento = repoEst.findByIdEstablecimiento(est.getIdEstablecimiento());
                if(establecimiento.isPresent()){
                    if(est.getNombre() != nuevoNombre)
                        est.setNombre(nuevoNombre);
                    if(est.getTelefono() != nuevoTelefono)
                        est.setTelefono(nuevoTelefono);
                    if(est.getLocalidad() != nuevaLocalidad)
                        est.setLocalidad(nuevaLocalidad);
                    if(est.getProvincia() != nuevaProvincia)
                        est.setProvincia(nuevaProvincia);
                    if(est.getCalle() != nuevaCalle)
                        est.setCalle(nuevaCalle);
                    if(est.getCodPostal() != nuevoCodPostal)
                        est.setCodPostal(nuevoCodPostal);
                    if(est.getPais() != nuevoPais)
                        est.setPais(nuevoPais);
                    repoEst.actualizar(est);
                    return true;
                }else{
                    throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
                }
            }else{
                throw new NoPermisosException("El usuario no tiene permisos para realizar esa acción.");
            }
        }
        throw new SesionNoIniciadaException("El usuario no ha iniciado sesion.");
    }

    public boolean crearActividad(Actividad actividad) throws ActividadNoCreada {
        Actividad acti = repoAct.save(actividad);
        if(acti == null)
            throw new ActividadNoCreada("La actividad no se pudo crear.");
        return true;

    }

    public boolean comentarEstablecimiento(Usuario comentador, Establecimiento estComentado, Comentario com) throws UsuarioNoExisteException, SesionNoIniciadaException, EstablecimientoNoExistenteException, ActividadNoCreada {
        Optional<Usuario> usuario = repoUsuario.findByDni(comentador.getDni());
        if(usuario.isPresent()){
            if(comentador.isSesionIniciada()){
                Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(estComentado.getIdEstablecimiento());
                if(est.isPresent()){
                    repoCom.save(com);
                    estComentado.anadirComentario(com);
                    comentador.anadirComentario(com);
                    repoEst.actualizar(estComentado);
                    repoUsuario.actualizarUsuario(comentador);
                    Actividad act = new Actividad(comentador, estComentado, MensajePredefinido.HA_COMENTADO);
                    crearActividad(act);
                    comentador.anadirActividad(act);
                    return true; //El comentario se ha realizado con éxito
                }else{
                    throw new EstablecimientoNoExistenteException("El establecimiento al que desea comentar no existe.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("Registrese para realizar el comentario.");
        }
    }

    public boolean editarComentario(Usuario comentador, Comentario com, String nuevoComentario) throws UsuarioNoExisteException, SesionNoIniciadaException, EstablecimientoNoExistenteException, ActividadNoCreada, ComentarioNoExiste {
        Optional<Usuario> usuario = repoUsuario.findByDni(comentador.getDni());
        if(usuario.isPresent()){
            if(comentador.isSesionIniciada()){
                Optional<Comentario> comentario = repoCom.findById(com.getId());
                if(comentario.isPresent()) {
                    if(!com.getMensaje().equals(nuevoComentario)) {
                        com.setMensaje(nuevoComentario);
                        repoCom.actualizar(com);
                        return true;
                    }
                }else{
                    throw new ComentarioNoExiste("El comentario no existe");
                }
            }else{
                throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("Registrese para realizar el comentario.");
        }
        return false;
    }

    public boolean eliminarComentarioEstablecimiento(Usuario usuGestor, Comentario com, Establecimiento establecimiento) throws UsuarioNoExisteException, ComentarioNoExiste, ActividadNoCreada, SesionNoIniciadaException, EstablecimientoNoExistenteException {
        Optional<Usuario> usu = repoUsuario.findByDni(usuGestor.getDni());
        if (usu.isPresent()) {
            if (usu.get().isSesionIniciada()) {
                Optional<Establecimiento> establecimiento1 = repoEst.findByIdEstablecimiento(establecimiento.getIdEstablecimiento());
                if (establecimiento1.isPresent()) {
                    Optional<Comentario> comentario = repoCom.findById(com.getId());
                    if (comentario.isPresent()) {
                        establecimiento.eliminarComentario(com);
                        return true; // Comentario eliminado satisfactoriamente
                    } else {
                        throw new ComentarioNoExiste("El comentario no existe.");
                    }
                } else {
                    throw new EstablecimientoNoExistenteException("El establecimiento ni siquiera existe.");
                }
            } else {
                throw new SesionNoIniciadaException("Sesión no iniciada.");
            }
        }
        throw new UsuarioNoExisteException("El usuario no está registrado.");
    }



    @Transactional
    public boolean visitarEstablecimiento(Usuario visitante, Establecimiento estVisitado) throws UsuarioNoExisteException, SesionNoIniciadaException, ActividadNoCreada, EstablecimientoNoExistenteException {
        Optional<Usuario> usu = repoUsuario.findByDni(visitante.getDni());
        if(usu.isPresent()){
            if(visitante.isSesionIniciada()){
                Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(estVisitado.getIdEstablecimiento());
                if(est.isPresent()){
                    visitante.visitarEstablecimiento(estVisitado);
                    Actividad act = new Actividad(visitante, estVisitado, MensajePredefinido.HA_VISITADO);
                    crearActividad(act);
                    visitante.anadirActividad(act);
                    return true;
                }else{
                    throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuairo no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public boolean marcarEstablecimientoFavorito(Usuario visitante, Establecimiento estVisitado) throws UsuarioNoExisteException, SesionNoIniciadaException, ActividadNoCreada, EstablecimientoNoExistenteException {
        Optional<Usuario> usu = repoUsuario.findByDni(visitante.getDni());
        if(usu.isPresent()){
            if(visitante.isSesionIniciada()){
                Optional<Establecimiento> est = repoEst.findById(estVisitado.getIdEstablecimiento());
                if(est.isPresent()){
                    visitante.anadirEstablecimientoFavorito(estVisitado);
                    return true;
                }else{
                    throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuairo no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public boolean darLikeEstablecimiento(Usuario visitante, Establecimiento estVisitado) throws UsuarioNoExisteException, SesionNoIniciadaException, ActividadNoCreada, EstablecimientoNoExistenteException {
        Optional<Usuario> usu = repoUsuario.findByDni(visitante.getDni());
        if(usu.isPresent()){
            if(visitante.isSesionIniciada()){
                Optional<Establecimiento> est = repoEst.findById(estVisitado.getIdEstablecimiento());
                if(est.isPresent()){
                    estVisitado.sumarLike();
                    repoEst.actualizar(estVisitado);
                    Actividad actividad = new Actividad(visitante, estVisitado, MensajePredefinido.HA_DADO_LIKE);
                    crearActividad(actividad);
                    visitante.anadirActividad(actividad);
                    repoUsuario.actualizarUsuario(visitante);
                    return true;
                }else{
                    throw new EstablecimientoNoExistenteException("El establecimiento no existe.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuairo no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    public boolean seguirUsuario(Usuario sigueA, Usuario seguido) throws UsuarioNoExisteException, SesionNoIniciadaException {
        Optional<Usuario> usu = repoUsuario.findByDni(sigueA.getDni());
        if(usu.isPresent()){
            if(sigueA.isSesionIniciada()){
                Optional<Usuario> seguir = repoUsuario.findByDni(seguido.getDni());
                if(seguir.isPresent()){
                    sigueA.seguirUsuario(seguido);
                    repoUsuario.actualizarUsuario(seguido);
                    seguido.anadirSeguidor(sigueA);
                    repoUsuario.actualizarUsuario(sigueA);
                    return true;
                }else{
                    throw new UsuarioNoExisteException("El usuario que desea seguir no existe.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no está registrado.");
        }
    }


    public boolean comentarComentario(Usuario comentador, Comentario comPadre, Comentario comHijo, int modo, String nuevoMensaje) throws SesionNoIniciadaException, UsuarioNoExisteException, ComentarioNoExiste {
        Optional<Usuario> usu = repoUsuario.findByDni(comentador.getDni());
        if(usu.isPresent()){
            if(comentador.isSesionIniciada()){
                Optional<Comentario> comen = repoCom.findById(comPadre.getId());
                if(comen.isPresent()){
                    if(modo == 1) {
                        //AñadirComentario
                        comPadre.anadirComentario(comHijo);
                        comHijo.setComentarioPadre(comPadre);
                        comentador.anadirComentario(comHijo);
                        repoCom.actualizar(comHijo);
                        repoCom.actualizar(comPadre);
                        return true;
                    } else if (modo == 2) {
                        //EliminarComentario
                        Optional<Comentario> comen2 = repoCom.findById(comHijo.getId());
                        if(comen2.isPresent()) {
                            comPadre.quitarComentario(comHijo);
                            repoCom.save(comPadre);
                            repoCom.delete(comHijo);
                        }else{
                            throw new ComentarioNoExiste("El comentario no eiste.");
                        }
                    } else if (modo == 3) {
                        Optional<Comentario> comen2 = repoCom.findById(comHijo.getId());
                        if(comen2.isPresent()) {
                            if(comHijo.getMensaje()!=nuevoMensaje) {
                                comHijo.setMensaje(nuevoMensaje);
                                repoCom.save(comHijo);
                                return true;
                            }
                        }else{
                            throw new ComentarioNoExiste("El comentario no eiste.");
                        }
                    }
                }else{
                    throw new ComentarioNoExiste("El comentario no eiste.");
                }
            }else{
                throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no está registrado.");
        }
        return false;
    }

    public boolean gestionUsuario(Usuario usu, int modo, Usuario gestionado, String nombre, String apellidos, String email, String password, LocalDate fnac) throws UsuarioNoExisteException, SesionNoIniciadaException, NoPermisosException {
        Optional<Usuario> usuarioGestor = repoUsuario.findByDni(usu.getDni());
        Optional<Usuario> usuarioGestionado = repoUsuario.findByDni(gestionado.getDni());

        if(usuarioGestor.isPresent()) {
            if (usuarioGestionado.isPresent()) {
                if (modo == 1) { //Eliminamos usuario, solo lo puede hacer el admin
                    if (usu.isSesionIniciada()) {
                        if (usu.getRol() == Rol.ADMIN && usu != gestionado) {
                            gestionado.setArchivado(true);
                            repoUsuario.save(gestionado);
                        }
                        throw new NoPermisosException("El usuario no tiene permisos para realizar esta acción.");
                    }
                    throw new SesionNoIniciadaException("El usuario no ha iniciado sesión");

                } else if (modo == 2) {
                    //Modificamos datos del usuario
                    if (gestionado.isSesionIniciada()) {
                        if (!usu.getNombre().equals(nombre)) {
                            usu.setNombre(nombre);
                        }
                        if (!usu.getApellidos().equals(apellidos)) {
                            usu.setApellidos(apellidos);
                        }
                        if (!usu.getPassword().equals(email)) {
                            usu.setPassword(password);
                        }
                        if (usu.getFechaNacimiento() != fnac) {
                            usu.setFechaNacimiento(fnac);
                        }
                        repoUsuario.save(gestionado);
                        return true;
                    }else{
                        throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
                    }
                } else if (modo == 3) {
                    //El usuario quiere dar de baja su cuenta
                    if (gestionado.isSesionIniciada()) {
                        gestionado.setArchivado(true);
                        return true;
                    }else{
                        throw new SesionNoIniciadaException("El usuario no ha iniciado sesión.");
                    }
                }
            }else{
                throw new UsuarioNoExisteException("El usuario no existe.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
        return false;
    }


    public Usuario loginUsuario(LoginDTO loginDTO) throws UsuarioNoExisteException, ContrasenaIncorrectaException {
        String msg = "";
        Usuario usuario = repoUsuario.findByEmail(loginDTO.getEmail()).get();
        if(usuario != null){
            String password = loginDTO.getPassword();
            String encodedPassword = usuario.getPassword();

            if(bCryptPasswordEncoder.matches(password, encodedPassword)){
                Optional<Usuario> usu = repoUsuario.findOneByEmailAndPassword(loginDTO.getEmail(), encodedPassword);
                if(usu.isPresent()){
                    usu.get().setSesionIniciada(true);
                    usu.get().setSesionCerrada(false);
                    repoUsuario.actualizarUsuario(usu.get());
                    return usu.get();
                }else{
                    throw new UsuarioNoExisteException("El usuario no existe.");
                }
            }else{
                throw new ContrasenaIncorrectaException("La contraseña no es correcta.");
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

}