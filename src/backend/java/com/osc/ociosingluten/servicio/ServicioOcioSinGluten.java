package com.osc.ociosingluten.servicio;

import com.osc.ociosingluten.excepciones.ContrasenaIncorrectaException;
import com.osc.ociosingluten.excepciones.SesionNoIniciadaException;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.excepciones.UsuarioNoExisteException;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ssl.SslProperties;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

import static constantes.Constantes.MaxlargoContrasena;

@Service
public class ServicioOcioSinGluten {

    @Autowired
    UsuarioRepository repoUsuario;

    @Autowired
    CacheManager cacheManager;

    public ServicioOcioSinGluten() {
    }

    /**
     * Funcion utilizada para el registro del usuario, el registro se simula añadiendo un usuario a la base de datos
     * @param usu Usuario a registrar
     * @return True si se ha registrado correctamente
     * @throws UsuarioExisteException si ese usuario existe en la base de datos
     */
    public boolean registroUsuario(@NotNull @Valid Usuario usu) throws UsuarioExisteException {
        Optional<Usuario> prueba = repoUsuario.findByDni(usu.getDni());
        Optional<Usuario> prueba2 = repoUsuario.findByEmail(usu.getEmail());
        if(prueba.isPresent() && prueba2.isPresent())
            throw new UsuarioExisteException("El usuario ya existe.");
        repoUsuario.save(usu);
        return true;
    }

    public Usuario loginUsuario(@NotBlank String email, @NotBlank String password) throws UsuarioNoExisteException, ContrasenaIncorrectaException {
        Optional<Usuario> usu = repoUsuario.findByEmail(email);

        if(usu.isPresent()){
            Usuario usuario = usu.get();
            if(usuario.getPassword().equals(password)){
                usuario.setSesionCerrada(false);
                usuario.setSesionIniciada(true);
                repoUsuario.actualizarUsuario(usuario);
                return usuario;
            }else{
                throw new ContrasenaIncorrectaException("La contraseña no es correcta");
            }
        }else{
            throw new UsuarioNoExisteException("No existe ese email registrado.");
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


    public List<Usuario> buscarUsuario(Usuario usuario) throws UsuarioNoExisteException {
        List<Usuario> usuarios = repoUsuario.findByUsername(usuario.getUsername());
        if(!usuarios.isEmpty()) {
            return usuarios; // Por ejemplo, podrías devolver el primer usuario de la lista
        } else {
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }






}