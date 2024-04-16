package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.ContrasenaIncorrectaException;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.excepciones.UsuarioNoExisteException;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
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
import java.util.stream.Collectors;
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
    @PostMapping("/nuevoUsuario")
    public ResponseEntity<UsuarioDTO> anadirUsuario(@RequestParam("dni") String dni,
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
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDTO(usuario));
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    @GetMapping("/perfilUsuarioUsername/{username}")
    public ResponseEntity<List<UsuarioDTO>> mostrarUsuarioporUsername(@PathVariable String username){
        List<Usuario> usuarios = repoUsu.findByUsernameContaining(username);
        if (!usuarios.isEmpty()) {
            List<UsuarioDTO> usuariosDTO = usuarios.stream()
                    .map(UsuarioDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(usuariosDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
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

    @GetMapping("/perfilUsuario/{username}/seguidores")
    public ResponseEntity<List<UsuarioDTO>> obtenerSeguidores(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            List<UsuarioDTO> seguidoresDTO = usuarioOptional.get().getSeguidores().stream()
                    .map(UsuarioDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(seguidoresDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/perfilUsuario/{username}/seguidos")
    public ResponseEntity<List<UsuarioDTO>> obtenerSeguidos(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            List<UsuarioDTO> seguidosDTO = usuarioOptional.get().getSeguidos().stream()
                    .map(UsuarioDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(seguidosDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // En el controlador UsuarioController

    @GetMapping("/perfilUsuario/{username}/actividades")
    public ResponseEntity<List<Actividad>> obtenerActividadesPorUsuario(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            List<Actividad> actividades = usuarioOptional.get().getActividades();
            return ResponseEntity.ok(actividades);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/perfilUsuario/{username}/estFavoritos")
    public ResponseEntity<List<Establecimiento>> obtenerEstablecimientosFavoritosPorUsuario(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            List<Establecimiento> establecimientos = usuarioOptional.get().getEstablecimientosFavoritos();
            return ResponseEntity.ok(establecimientos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/perfilUsuario/{username}/estVisitados")
    public ResponseEntity<List<Establecimiento>> obtenerEstablecimientosVisitadosPorUsuario(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            List<Establecimiento> establecimientos = usuarioOptional.get().getEstablecimientosVisitados();
            return ResponseEntity.ok(establecimientos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
