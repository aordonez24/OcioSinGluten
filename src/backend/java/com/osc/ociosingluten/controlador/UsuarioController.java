package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.excepciones.UsuarioNoExisteException;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.apache.coyote.Response;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.zip.DeflaterOutputStream;

@RestController
@RequestMapping("/ociosingluten/usuarios")
@CrossOrigin("http://localhost:3000")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repoUsu;

    @Autowired
    private ServicioOcioSinGluten servicio;

    //Obtener todos los usuarios que se encuentran registrados en la web, esto solo lo podría ver un usuario admin
    @GetMapping("/listadoUsuarios")
    public List<Usuario> cargarTodosUsuarios(){
        return repoUsu.findAll();
    }

    //Añadir el usuario
    @PostMapping("/altaUsuario")
    public Usuario anadirUsuario(@RequestParam("dni") String dni,
                              @RequestParam("username") String username,
                              @RequestParam("nombre") String nombre,
                              @RequestParam("apellidos") String apellidos,
                              @RequestParam("fechaNacimiento") LocalDate fechaNacimiento,
                              @RequestParam("telefono") int telefono,
                              @RequestParam("fotoPerfil") byte[] fotoPerfil,
                              @RequestParam("email") String email,
                              @RequestParam("password") String password) throws IOException, UsuarioExisteException {
        Usuario usuario = new Usuario(dni, username, nombre, apellidos, fechaNacimiento, telefono, compress(fotoPerfil), email, password);
        if(servicio.registroUsuario(usuario)){
            return usuario;
        }else{
            return null;
        }

    }


    @GetMapping("/perfilUsuarioUsername/{username}")
    public ResponseEntity<UsuarioDTO> mostrarUsuarioporUsername(@PathVariable String username){
        Optional<Usuario> usuario = repoUsu.findByDni(username);
        return usuario.map(c -> ResponseEntity.ok(new UsuarioDTO(c))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/perfilUsuarioDni/{dni}")
    public ResponseEntity<UsuarioDTO> mostrarUsuarioporDni(@PathVariable String dni){
        Optional<Usuario> usuario = repoUsu.findByDni(dni);
        return usuario.map(c -> ResponseEntity.ok(new UsuarioDTO(c))).orElse(ResponseEntity.notFound().build());
    }



    private byte[] compress(byte[] input) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             LZMACompressorOutputStream lzmaOutputStream = new LZMACompressorOutputStream(outputStream)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                lzmaOutputStream.write(buffer, 0, bytesRead);
            }
            lzmaOutputStream.finish();

            return outputStream.toByteArray();
        }
    }
}
