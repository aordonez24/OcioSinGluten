package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.EstablecimientoEditDTO;
import com.osc.ociosingluten.controlador.DTO.NuevoComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.herramientas.MensajePredefinido;
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

import static com.osc.ociosingluten.herramientas.MensajePredefinido.HA_DADO_LIKE;
import static com.osc.ociosingluten.herramientas.MensajePredefinido.HA_VISITADO;

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

    @Autowired
    private ActividadRepository repoAct;

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
            if (!establecimiento.getImagenes().isEmpty()) {
                List<String> imagenesBase64 = establecimiento.getImagenes().stream()
                        .map(imagen -> convertirBytesAStringBase64(imagen.getImagen()))
                        .collect(Collectors.toList());
                EstablecimientoDTO establecimientoDTO = new EstablecimientoDTO(establecimiento.getIdEstablecimiento(),
                        establecimiento.getNombre(), establecimiento.getTelefono(), establecimiento.getLocalidad(),
                        establecimiento.getProvincia(), establecimiento.getCalle(), establecimiento.getCodPostal(),
                        establecimiento.getPais(), establecimiento.getNumLikes(), imagenesBase64, establecimiento.getVisitantes());
                return ResponseEntity.ok(establecimientoDTO);
            } else {
                EstablecimientoDTO establecimientoDTO = new EstablecimientoDTO(establecimiento.getIdEstablecimiento(),
                        establecimiento.getNombre(), establecimiento.getTelefono(), establecimiento.getLocalidad(),
                        establecimiento.getProvincia(), establecimiento.getCalle(), establecimiento.getCodPostal(),
                        establecimiento.getPais(), establecimiento.getNumLikes(), Collections.emptyList(), establecimiento.getVisitantes());
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
        if(est.isPresent()) {
            Imagen img = new Imagen(fotoPerfil, est.get());
            repoImg.save(img);

            est.get().anadirImagen(img);
            repoEst.actualizar(est.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(new EstablecimientoDTO(est.get()));
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/establecimientoFoto/{id}/fotomenos")
    public ResponseEntity<?> quitaFoto(@PathVariable int id, @RequestBody String  fotoPerfilBase64){
        byte[] fotoPerfil = Base64.getDecoder().decode(fotoPerfilBase64);
        Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(id);
        List<Imagen> imagenes = repoImg.findByImagenAndEstablecimiento(fotoPerfil, est.get());
        if(est.isPresent() && !imagenes.isEmpty()) {
            Imagen img = imagenes.get(imagenes.size()-1
             );
            repoImg.delete(img);
            repoEst.actualizar(est.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(new EstablecimientoDTO(est.get()));
        }else{
            return ResponseEntity.notFound().build();
        }
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
        Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(id);
        if (!est.isEmpty()) {
            Establecimiento establecimiento = est.get();
            List<Comentario> comentarios = establecimiento.getComentarios();

            return ResponseEntity.ok(comentarios);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/nuevoLike")
    public ResponseEntity<?> agregarLike(@PathVariable int id, @RequestBody String username) throws ActividadNoCreada {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usuario = repoUsu.findByUsername(username);
        if(establecimiento.get().getUsuariosQueDieronLike().contains(usuario.get())){
            return ResponseEntity.accepted().build();
        }
        if (establecimiento.isPresent()) {
            Establecimiento com = establecimiento.get();
            com.setNumLikes(com.getNumLikes() + 1, usuario.get(), 1);
            repoEst.actualizar(com);
            Usuario visitante = usuario.get();
            Actividad actividad = new Actividad(visitante, com, HA_DADO_LIKE);
            crearActividad(actividad);
            visitante.anadirActividad(actividad);
            return ResponseEntity.ok("Like con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/likemenos")
    public ResponseEntity<?> quitarLike(@PathVariable int id, @RequestBody String username) throws ActividadNoCreada {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usuario = repoUsu.findByUsername(username);
        if (establecimiento.isPresent() && establecimiento.get().getUsuariosQueDieronLike().contains(usuario.get())) {
            Optional<Actividad> act = repoAct.findByEstablecimientoAndMensajePredefinidoAndAutor(establecimiento.get(), HA_DADO_LIKE, usuario.get());
            usuario.get().getActividades().remove(act.get());
            repoAct.delete(act.get());
            Establecimiento com = establecimiento.get();
            com.setNumLikes(com.getNumLikes() - 1, usuario.get(), 2);
            establecimiento.get().getUsuariosQueDieronLike().remove(usuario.get());
            repoEst.actualizar(com);
            return ResponseEntity.ok("Like quitado con exito.");
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
    public ResponseEntity<?> agregarVisitado(@PathVariable int id, @RequestBody String username) throws ActividadNoCreada {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (establecimiento.isPresent()) {
            usu.get().visitarEstablecimiento(establecimiento.get());
            repoUsu.actualizarUsuario(usu.get());
            establecimiento.get().anadirVisitante(usu.get());
            repoEst.actualizar(establecimiento.get());
            Actividad act = new Actividad(usu.get(), establecimiento.get(), MensajePredefinido.HA_VISITADO);
            crearActividad(act);
            usu.get().anadirActividad(act);
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

    @DeleteMapping("/{id}/noVisitado")
    public ResponseEntity<?> eliminarVisitado(@PathVariable int id, @RequestParam String username) {
        Optional<Establecimiento> establecimiento = repoEst.findById(id);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (establecimiento.isPresent()) {
            usu.get().getEstablecimientosVisitados().remove(establecimiento.get());
            Optional<Actividad> act = repoAct.findByEstablecimientoAndMensajePredefinidoAndAutor(establecimiento.get(), HA_VISITADO, usu.get());
            usu.get().getActividades().remove(act.get());
            establecimiento.get().getVisitantes().remove(usu.get());
            repoEst.actualizar(establecimiento.get());
            repoAct.delete(act.get());
            repoUsu.actualizarUsuario(usu.get());
            return ResponseEntity.ok("Favorito con exito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PutMapping("/establecimientos/{id}/modEstablecimiento")
    public ResponseEntity<EstablecimientoDTO> modificarUsuarioDesdeFormulario(@PathVariable int id,
                                                                              @RequestBody EstablecimientoEditDTO requestData) {
        Optional<Establecimiento> establecimiento = repoEst.findByIdEstablecimiento(id);
        if (establecimiento.isPresent()) {
            Establecimiento est = establecimiento.get();

            est.setNombre(requestData.getNombre());
            est.setPais(requestData.getPais());
            est.setTelefono(Integer.parseInt(requestData.getTelefono()));
            est.setCodPostal(Integer.parseInt(requestData.getCodPostal()));
            est.setCalle(requestData.getCalle());
            est.setProvincia(requestData.getProvincia());
            est.setLocalidad(requestData.getLocalidad());

            repoEst.save(est);

            return ResponseEntity.ok(new EstablecimientoDTO(est));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/establecimientos/{id}/nuevoComentario")
    public ResponseEntity<EstablecimientoDTO> agregarComentario(@PathVariable int id,
                                                                @RequestBody NuevoComentarioDTO comentario) throws ActividadNoCreada {
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

                Actividad act = new Actividad(usu.get(), establecimiento, MensajePredefinido.HA_COMENTADO);
                crearActividad(act);
                usu.get().anadirActividad(act);
                repoUsu.actualizarUsuario(usu.get());
            }
            return ResponseEntity.ok(new EstablecimientoDTO(establecimiento));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public boolean crearActividad(Actividad actividad) throws ActividadNoCreada {
        Actividad acti = repoAct.save(actividad);
        if(acti == null)
            throw new ActividadNoCreada("La actividad no se pudo crear.");
        return true;

    }

    @DeleteMapping("/establecimientos/{id}")
    public ResponseEntity<?> eliminarEstablecimiento(@PathVariable int id) {
        Optional<Establecimiento> establecimientoOpt = repoEst.findById(id);
        if (establecimientoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Establecimiento establecimiento = establecimientoOpt.get();

        if (establecimiento.getImagenes() != null) {
            repoImg.deleteAll(establecimiento.getImagenes());
        }

        for (Comentario comentario : establecimiento.getComentarios()) {
            Usuario autor = comentario.getAutor();
            if (autor != null) {
                autor.getComentariosRealizados().remove(comentario);
                repoUsu.save(autor);
            }
        }
        List<Comentario> comentariosHechos =new ArrayList<>();
        comentariosHechos.addAll(establecimiento.getComentarios());
        establecimiento.getComentarios().clear();
        for(Comentario com : comentariosHechos){
            comRe.delete(com);
        }

        List<Actividad> actividades = repoAct.findByEstablecimiento(establecimiento);

        for (Actividad actividad : actividades) {
            Usuario autor = actividad.getAutor();
            if (autor != null) {
                autor.getActividades().remove(actividad);
                repoUsu.actualizarUsuario(autor);
            }
        }

        for(Actividad act : actividades){
            repoAct.delete(act);
        }

        List<Usuario> usuarios = repoUsu.findByEstablecimientosFavoritosOrEstablecimientosVisitados(establecimiento, establecimiento);
        for (Usuario usuario : usuarios) {
            usuario.getEstablecimientosFavoritos().remove(establecimiento);
            usuario.getEstablecimientosVisitados().remove(establecimiento);
            repoUsu.save(usuario);
        }

        repoEst.delete(establecimiento);

        return ResponseEntity.ok().build();
    }

}

