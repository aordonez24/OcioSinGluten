package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.NuevoComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.modelo.*;
import com.osc.ociosingluten.repositorio.*;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.apache.commons.compress.compressors.lzma.LZMACompressorOutputStream;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ociosingluten/establecimientos")
@CrossOrigin("http://localhost:3000")
public class EstablecimientoController {

    @Autowired
    private EstablecimientoRepository repoEst;

    @Autowired
    private ImagenRepository repoImg;

    @Autowired
    private ServicioOcioSinGluten servicio;

    @Autowired
    private ComentarioRepository comRe;

    @Autowired
    private UsuarioRepository repoUsu;

    @GetMapping("/listadoEstablecimientos")
    public List<Establecimiento> cargarTodosLosEstablecimientos(){
        return repoEst.findAll();
    }

    @GetMapping("/establecimientos/{nombre}")
    public List<Establecimiento> cargarEstxNombre(@PathVariable String nombre){
        return repoEst.findByNombreContaining(nombre);
    }

    @GetMapping("/establecimiento/{id}")
    public ResponseEntity<EstablecimientoDTO> cargarEstxId(@PathVariable int id) {
        Optional<Establecimiento> establecimientoOptional = repoEst.findByIdEstablecimiento(id);
        if (establecimientoOptional.isPresent()) {
            Establecimiento establecimiento = establecimientoOptional.get();
            // Verificar si el establecimiento tiene imágenes
            if (!establecimiento.getImagenes().isEmpty()) {
                // Convertir las imágenes a cadenas Base64
                List<String> imagenesBase64 = establecimiento.getImagenes().stream()
                        .map(imagen -> convertirBytesAStringBase64(imagen.getImagen()))
                        .collect(Collectors.toList());
                // Crear el EstablecimientoDTO con las imágenes en Base64
                EstablecimientoDTO establecimientoDTO = new EstablecimientoDTO(establecimiento.getIdEstablecimiento(),
                        establecimiento.getNombre(), establecimiento.getTelefono(), establecimiento.getLocalidad(),
                        establecimiento.getProvincia(), establecimiento.getCalle(), establecimiento.getCodPostal(),
                        establecimiento.getPais(), establecimiento.getNumLikes(), imagenesBase64);
                return ResponseEntity.ok(establecimientoDTO);
            } else {
                // Si el establecimiento no tiene imágenes, devolver el DTO sin las imágenes
                EstablecimientoDTO establecimientoDTO = new EstablecimientoDTO(establecimiento.getIdEstablecimiento(),
                        establecimiento.getNombre(), establecimiento.getTelefono(), establecimiento.getLocalidad(),
                        establecimiento.getProvincia(), establecimiento.getCalle(), establecimiento.getCodPostal(),
                        establecimiento.getPais(), establecimiento.getNumLikes(), Collections.emptyList());
                return ResponseEntity.ok(establecimientoDTO);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private String convertirBytesAStringBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }


    @PostMapping("/establecimientoFoto/{id}/nuevaFoto")
    public ResponseEntity<?> nuevaFoto(@PathVariable int id, @RequestBody String  fotoPerfilBase64){
        byte[] fotoPerfil = Base64.getDecoder().decode(fotoPerfilBase64);

        Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(id);
        Imagen img = new Imagen(fotoPerfil, est.get());
        repoImg.save(img);

        est.get().anadirImagen(img);
        repoEst.actualizar(est.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(new EstablecimientoDTO(est.get()));
    }

    @PostMapping("/nuevoEstablecimiento")
    public ResponseEntity<EstablecimientoDTO> anadirEstablecimiento(@RequestParam("nombre") String nombre,
                                                    @RequestParam("telefono") int telefono,
                                                    @RequestParam("localidad") String localidad,
                                                    @RequestParam("provincia") String provincia,
                                                    @RequestParam("calle") String calle,
                                                    @RequestParam("codPostal") int codPostal,
                                                    @RequestParam("pais") String  pais,
                                                    @RequestParam String username) throws IOException, UsuarioExisteException, UsuarioNoExisteException, EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException {
        //Username es el usuario con el que he iniciado sesión para que introduzca el establecimiento
        Usuario usuarioqueAnade = servicio.buscarUsuarioXUsername(username);
        Establecimiento est = new Establecimiento(nombre, telefono, localidad, provincia, calle, codPostal, pais);

        if(servicio.publicarEstablecimiento(usuarioqueAnade, est)){
            return ResponseEntity.status(HttpStatus.CREATED).body(new EstablecimientoDTO(est));
        }else{
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

    }

    @GetMapping("/establecimientos/{id}/comentarios")
    public ResponseEntity<List<Comentario>> cargarComentariosEst(@PathVariable int id){
        // Buscar el establecimiento por nombre
        Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(id);

        if (!est.isEmpty()) {
            Establecimiento establecimiento = est.get(); // Suponiendo que solo obtienes un establecimiento

            // Obtener la lista de comentarios del establecimiento
            List<Comentario> comentarios = establecimiento.getComentarios();

            return ResponseEntity.ok(comentarios);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/nuevoLike")
    public ResponseEntity<?> agregarLike(@PathVariable int id) {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);

        if (establecimiento.isPresent()) {
            Establecimiento com = establecimiento.get();
            com.setNumLikes(com.getNumLikes() + 1);
            repoEst.actualizar(com);
            return ResponseEntity.ok("Like con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/favorito")
    public ResponseEntity<?> agregarFavorito(@PathVariable int id, @RequestBody String username) {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (establecimiento.isPresent()) {
            usu.get().anadirEstablecimientoFavorito(establecimiento.get());
            repoUsu.actualizarUsuario(usu.get());
            return ResponseEntity.ok("Favorito con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/visitado")
    public ResponseEntity<?> agregarVisitado(@PathVariable int id, @RequestBody String username) {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (establecimiento.isPresent()) {
            usu.get().visitarEstablecimiento(establecimiento.get());
            repoUsu.actualizarUsuario(usu.get());
            return ResponseEntity.ok("Visitado con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}/nofavorito")
    public ResponseEntity<?> eliminarFavorito(@PathVariable int id, @RequestParam String username) {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (establecimiento.isPresent()) {
            usu.get().eliminarEstablecimientoFavorito(establecimiento.get());
            repoUsu.actualizarUsuario(usu.get());
            return ResponseEntity.ok("Favorito con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/establecimientos/{id}/modEstablecimiento")
    public ResponseEntity<EstablecimientoDTO> modificarUsuarioDesdeFormulario(@PathVariable int id,
                                                                              @RequestBody MultiValueMap<String, String> formData) {
        Optional<Establecimiento> establecimiento = repoEst.findByIdEstablecimiento(id);
        if (establecimiento.isPresent()) {
            Establecimiento est = establecimiento.get();

            est.setNombre(formData.getFirst("nombre"));
            est.setPais(formData.getFirst("pais"));
            est.setTelefono(Integer.parseInt(formData.getFirst("telefono")));
            est.setCodPostal(Integer.parseInt(formData.getFirst("codPostal")));
            est.setCalle(formData.getFirst("calle"));
            est.setProvincia(formData.getFirst("provincia"));
            est.setLocalidad(formData.getFirst("localidad"));

            repoEst.save(est);

            return ResponseEntity.ok(new EstablecimientoDTO(est));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/establecimientos/{id}/nuevoComentario")
    public ResponseEntity<EstablecimientoDTO> agregarComentario(@PathVariable int id,
                                                                @RequestBody NuevoComentarioDTO comentario) {
        Optional<Establecimiento> establecimientoOptional = repoEst.findByIdEstablecimiento(id);
        if (establecimientoOptional.isPresent()) {
            Establecimiento establecimiento = establecimientoOptional.get();

            Optional<Usuario> usu = repoUsu.findByUsername(comentario.getUsername());
            if(usu.isPresent()) {
                Comentario com = new Comentario(comentario.getMensaje(), usu.get());
                comRe.save(com);
                establecimiento.anadirComentario(com);
                repoEst.actualizar(establecimiento);
                usu.get().anadirComentario(com);
                repoUsu.actualizarUsuario(usu.get());
            }
            return ResponseEntity.ok(new EstablecimientoDTO(establecimiento));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}

