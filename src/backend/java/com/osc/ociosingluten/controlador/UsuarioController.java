package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.LoginDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.herramientas.LoginMessage;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import com.osc.ociosingluten.seguridad.CodificadorPassword;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.osc.ociosingluten.seguridad.UserService;
import com.osc.ociosingluten.seguridad.JwtTokenUtil;

import java.util.Base64;


@RestController
@RequestMapping("/ociosingluten/usuarios")
@CrossOrigin("/localhost:3000")
public class UsuarioController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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
                              @RequestParam("fotoPerfil") String  fotoPerfilBase64,
                              @RequestParam("email") String email,
                              @RequestParam("password") String password) throws IOException, UsuarioExisteException {
        byte[] fotoPerfil = Base64.getDecoder().decode(fotoPerfilBase64);
        Usuario usuario = new Usuario(dni, username, nombre, apellidos, fechaNacimiento, telefono, fotoPerfil, email, password);
        if(servicio.registroUsuario(usuario)){
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDTO(usuario));
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    @GetMapping("/perfilUsuarioUsername/{username}")
    public ResponseEntity<UsuarioDTO> mostrarUsuarioporUsername(@PathVariable String username) throws IOException {
        Optional<Usuario> usuario = repoUsu.findByUsername(username);
        if (!usuario.isEmpty()) {
            Usuario usu = usuario.get();
            String fotoPerfil = Base64.getEncoder().encodeToString(usu.getFotoPerfil());
            UsuarioDTO usuarioDTO = new UsuarioDTO(usu.getDni(), usu.getUsername(), usu.getNombre(), usu.getApellidos(), usu.getFechaNacimiento(), usu.getTelefono(), fotoPerfil, usu.getEmail(), usu.getPassword());
                    //String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, byte[] fotoPerfil, String email, String password
            return ResponseEntity.ok(usuarioDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Método para descomprimir los datos de la imagen
    private byte[] descomprimir(byte[] input) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             LZMACompressorInputStream lzmaInputStream = new LZMACompressorInputStream(inputStream)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = lzmaInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
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

    @PutMapping("/perfilUsuario/{username}/modUsuario")
    public ResponseEntity<UsuarioDTO> modificarUsuarioDesdeFormulario(@PathVariable String username,
                                                                      @RequestBody MultiValueMap<String, String> formData) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            if(usuario.isSesionIniciada()) {
                usuario.setPassword(formData.getFirst("password"));
                usuario.setNombre(formData.getFirst("nombre"));
                usuario.setApellidos(formData.getFirst("apellidos"));
                usuario.setFechaNacimiento(LocalDate.parse(formData.getFirst("fechaNacimiento")));
                usuario.setEmail(formData.getFirst("email"));
                usuario.setUsername(formData.getFirst("username"));
                usuario.setDni(formData.getFirst("dni"));

                repoUsu.save(usuario);
            }else{
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(new UsuarioDTO(usuario));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/usuarios/{dni}/nuevoEstablecimiento")
    public ResponseEntity<EstablecimientoDTO> anadirEstablecimiento(@RequestParam("nombre") String nombre,
                                                                    @RequestParam("localidad") String localidad,
                                                                    @RequestParam("provincia") String provincia,
                                                                    @RequestParam("calle") String calle,
                                                                    @RequestParam("codPostal") int codPostal,
                                                                    @RequestParam("telefono") int telefono,
                                                                    @RequestParam("fotos") List<byte[]> imagenes,
                                                                    @RequestParam("pais") String pais, @PathVariable String dni) throws IOException, UsuarioExisteException, UsuarioNoExisteException, EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException {
        Optional<Usuario> usuQueAnade = repoUsu.findByDni(dni);
        if(usuQueAnade.isPresent()) {
            Usuario usuario = usuQueAnade.get();
            if(usuario.isSesionIniciada()) {
                List<byte[]> imagenesComprimidas = new ArrayList<>();
                for (int i = 0; i < imagenes.size(); i++) {
                    imagenesComprimidas.add(compress(imagenes.get(i)));
                }

                Establecimiento establecimiento = new Establecimiento(nombre, telefono, localidad, provincia, calle, codPostal, pais, imagenesComprimidas);
                if (servicio.publicarEstablecimiento(usuario, establecimiento)) {
                    return ResponseEntity.status(HttpStatus.CREATED).body(new EstablecimientoDTO(establecimiento));
                } else {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
        }else{
            throw new UsuarioNoExisteException("El usuario no existe");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginDTO loginDTO) throws UsuarioNoExisteException, ContrasenaIncorrectaException {
        Optional<Usuario> usuarioLogeado = repoUsu.findByEmail(loginDTO.getEmail());
        Usuario usu = usuarioLogeado.get();
        String fotoPerfil = Base64.getEncoder().encodeToString(usu.getFotoPerfil());
        UsuarioDTO usuario = new UsuarioDTO(usu.getDni(), usu.getUsername(), usu.getNombre(), usu.getApellidos(), usu.getFechaNacimiento(), usu.getTelefono(), fotoPerfil, usu.getEmail(), usu.getPassword());
                //String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, byte[] fotoPerfil, String email, String password
        Usuario usuario1 = servicio.loginUsuario(loginDTO);
        return ResponseEntity.ok(usuario1);
    }

}
