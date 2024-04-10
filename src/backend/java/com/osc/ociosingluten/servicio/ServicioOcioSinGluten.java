package com.osc.ociosingluten.servicio;

import com.osc.ociosingluten.excepciones.ContrasenaIncorrectaException;
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
import java.util.Optional;

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

    public boolean loginUsuario(@NotBlank String email, @NotBlank String password) throws UsuarioNoExisteException, ContrasenaIncorrectaException {
        Optional<Usuario> usu = repoUsuario.findByEmail(email).stream().findFirst();

        if(usu.isPresent()){
            Usuario usuario = usu.get();
            if(usuario.getPassword().equals(password)){
                usuario.setSesionIniciada(true);
                return true;
            }else{
                throw new ContrasenaIncorrectaException("La contraseña no es correcta");
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






}