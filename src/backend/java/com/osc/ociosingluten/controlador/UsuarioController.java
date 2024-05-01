package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.LoginDTO;
import com.osc.ociosingluten.controlador.DTO.SeguirDTO;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.osc.ociosingluten.seguridad.UserService;
import com.osc.ociosingluten.seguridad.JwtTokenUtil;


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

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

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
        if (usuario.isPresent()) {
            Usuario usu = usuario.get();
            //String fotoPerfil = usu.getFotoPerfil() != null ? Base64.getEncoder().encodeToString(usu.getFotoPerfil()) : null;
            String fotoPerfil = "";
            if(usu.getFotoPerfil() != null){
                fotoPerfil = Base64.getEncoder().encodeToString(usu.getFotoPerfil());
            }
            UsuarioDTO usuarioDTO = new UsuarioDTO(usu.getDni(), usu.getUsername(), usu.getNombre(), usu.getApellidos(), usu.getFechaNacimiento(), usu.getTelefono(), fotoPerfil, usu.getEmail(), usu.getPassword());
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
            Usuario usuario = usuarioOptional.get();
            List<Usuario> seguidores = usuario.getSeguidores();
            if (seguidores != null) {
                List<UsuarioDTO> seguidoresDTO = seguidores.stream()
                        .map(seguidor -> {
                            // Verificar si el seguidor tiene una foto de perfil antes de mapearlo a UsuarioDTO
                            String fotoPerfil = seguidor.getFotoPerfil() != null ? Base64.getEncoder().encodeToString(seguidor.getFotoPerfil()) : null;
                            return new UsuarioDTO(seguidor.getDni(), seguidor.getUsername(), seguidor.getNombre(), seguidor.getApellidos(), seguidor.getFechaNacimiento(), seguidor.getTelefono(), fotoPerfil, seguidor.getEmail(), seguidor.getPassword());
                        })
                        .collect(Collectors.toList());
                return ResponseEntity.ok(seguidoresDTO);
            } else {
                // Si la lista de seguidores es null, devolver una lista vacía
                return ResponseEntity.ok(Collections.emptyList());
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    @GetMapping("/perfilUsuario/{username}/seguidos")
    public ResponseEntity<List<UsuarioDTO>> obtenerSeguidos(@PathVariable String username) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            List<Usuario> seguidos = usuario.getSeguidos();
            if (seguidos != null) {
                List<UsuarioDTO> seguidosDTO = seguidos.stream()
                        .map(seguido -> {
                            String fotoPerfil = seguido.getFotoPerfil() != null ? Base64.getEncoder().encodeToString(seguido.getFotoPerfil()) : null;
                            return new UsuarioDTO(seguido.getDni(), seguido.getUsername(), seguido.getNombre(), seguido.getApellidos(), seguido.getFechaNacimiento(), seguido.getTelefono(), fotoPerfil, seguido.getEmail(), seguido.getPassword());
                        })
                        .collect(Collectors.toList());
                return ResponseEntity.ok(seguidosDTO);
            } else {
                return ResponseEntity.ok(Collections.emptyList());
            }
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
                //String dni, String username, String nombre, String apellidos, LocalDate fechaNacimiento, int telefono, byte[] fotoPerfil, String email, String password
        if(usuarioLogeado.isPresent()) {
            Usuario usuario1 = servicio.loginUsuario(loginDTO);
            return ResponseEntity.ok(usuario1);
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    @PostMapping("/perfilUsuario/{username}/fotoPerfil")
    public ResponseEntity<String> actualizarFotoPerfil(@PathVariable String username,
                                                       @RequestBody String fotoPerfilBase64) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            try {
                byte[] fotoPerfil = Base64.getDecoder().decode(fotoPerfilBase64);
                usuario.setFotoPerfil(fotoPerfil);
                repoUsu.save(usuario);
                return ResponseEntity.ok("Foto de perfil actualizada exitosamente");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("La imagen proporcionada no es válida");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para actualizar la foto de perfil y los datos del usuario
    @PutMapping("/perfilUsuario/{username}/nuevosDatos")
    public ResponseEntity<String> actualizarPerfilUsuario(@PathVariable String username,
                                                          @RequestBody UsuarioDTO usuarioDTO) {
        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            try {
                // Verificar si se proporcionó un nuevo nombre de usuario y asegurarse de que no exista en la base de datos
                if (usuarioDTO.getUsername() != null && !usuarioDTO.getUsername().equals(username)) {
                    Optional<Usuario> otroUsuarioOptional = repoUsu.findByUsername(usuarioDTO.getUsername());
                    if (otroUsuarioOptional.isPresent()) {
                        return ResponseEntity.badRequest().body("El nuevo nombre de usuario ya está en uso");
                    }
                    usuario.setUsername(usuarioDTO.getUsername());
                }

                // Actualizar los datos del usuario si se han proporcionado
                if (usuarioDTO.getNombre() != null) {
                    usuario.setNombre(usuarioDTO.getNombre());
                }
                if (usuarioDTO.getApellidos() != null) {
                    usuario.setApellidos(usuarioDTO.getApellidos());
                }
                if (usuarioDTO.getFechaNacimiento() != null) {
                    usuario.setFechaNacimiento(usuarioDTO.getFechaNacimiento());
                }
                if (usuarioDTO.getTelefono() != 0) {
                    usuario.setTelefono(usuarioDTO.getTelefono());
                }

                // Guardar los cambios en la base de datos
                repoUsu.save(usuario);

                return ResponseEntity.ok("Perfil de usuario actualizado exitosamente");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Datos proporcionados inválidos");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/perfilUsuario/{username}/nuevaPassword")
    public ResponseEntity<String> cambiarContrasena(@PathVariable String username,
                                                    @RequestBody Map<String, String> data) {
        String nuevaContrasena = data.get("nuevaContrasena");

        Optional<Usuario> usuarioOptional = repoUsu.findByUsername(username);
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            try {
                usuario.setPassword(bCryptPasswordEncoder.encode(nuevaContrasena));
                repoUsu.save(usuario);

                return ResponseEntity.ok("Contraseña cambiada exitosamente");
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("La contraseña proporcionada no es válida");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/perfilUsuario/{username}/nuevoSeguidor")
    public ResponseEntity<?> seguirUsuario(@PathVariable String username, @RequestBody SeguirDTO seguirDTO) throws UsuarioNoExisteException {
        //Primero va el que desea seguir
        //Segundo el que va a ser seguido
        Optional<Usuario> usuarioquesigue = repoUsu.findByUsername(seguirDTO.getUsernameQueSigueA());
        Optional<Usuario> usuarioSeguido = repoUsu.findByUsername(seguirDTO.getUsernameAQuienSigue());

        if(usuarioquesigue.isPresent() && usuarioSeguido.isPresent()){
            Usuario sigueA = usuarioquesigue.get();
            Usuario Aeste = usuarioSeguido.get();

            sigueA.seguirUsuario(Aeste);
            repoUsu.actualizarUsuario(sigueA);
            Aeste.anadirSeguidor(sigueA);
            repoUsu.save(Aeste);

            return ResponseEntity.ok("Ahora estás siguiendo a " + Aeste.getUsername());
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

    @DeleteMapping("/perfilUsuario/{username}/seguidorMenos")
    public ResponseEntity<?> dejardeseguirUsuario(@PathVariable String username, @RequestBody SeguirDTO seguirDTO) throws UsuarioNoExisteException {
        //Primero va el que desea seguir
        //Segundo el que va a ser seguido
        Optional<Usuario> usuarioquesigue = repoUsu.findByUsername(seguirDTO.getUsernameQueSigueA());
        Optional<Usuario> usuarioSeguido = repoUsu.findByUsername(seguirDTO.getUsernameAQuienSigue());

        if(usuarioquesigue.isPresent() && usuarioSeguido.isPresent()){
            Usuario sigueA = usuarioquesigue.get();
            Usuario Aeste = usuarioSeguido.get();

            sigueA.dejarSeguir(Aeste);
            repoUsu.actualizarUsuario(sigueA);
            repoUsu.save(Aeste);

            return ResponseEntity.ok("Ahora estás siguiendo a " + Aeste.getUsername());
        }else{
            throw new UsuarioNoExisteException("El usuario no existe.");
        }
    }

}






