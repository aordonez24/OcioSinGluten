package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.modelo.*;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.EstablecimientoRepository;
import com.osc.ociosingluten.repositorio.ImagenRepository;
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
    public ResponseEntity<?> cargarEstxId(@PathVariable int id, @RequestBody String  fotoPerfilBase64){
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

    @GetMapping("/establecimientosProvincia/{provincia}")
    public List<Establecimiento> cargarEstxProvincia(@PathVariable String provincia){
        return repoEst.findByProvinciaContaining(provincia);
    }

    @GetMapping("/establecimientosLocalidad/{localidad}")
    public List<Establecimiento> cargarEstxLocalidad(@PathVariable String localidad){
        return repoEst.findByProvinciaContaining(localidad);
    }

    @GetMapping("/establecimientos/{nombre}/comentarios")
    public ResponseEntity<List<Comentario>> cargarComentariosEst(@PathVariable String nombre){
        // Buscar el establecimiento por nombre
        List<Establecimiento> establecimientos = repoEst.findByNombreContaining(nombre);

        if (!establecimientos.isEmpty()) {
            Establecimiento establecimiento = establecimientos.get(0); // Suponiendo que solo obtienes un establecimiento

            // Obtener la lista de comentarios del establecimiento
            List<Comentario> comentarios = establecimiento.getComentarios();

            return ResponseEntity.ok(comentarios);
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

        @PostMapping("/establecimientos/{id}/nuevoComentario")
        public ResponseEntity<EstablecimientoDTO> agregarComentario(@PathVariable int id,
                                                                    @RequestBody Comentario comentario) {
            Optional<Establecimiento> establecimientoOptional = repoEst.findByIdEstablecimiento(id);
            if (establecimientoOptional.isPresent()) {
                Establecimiento establecimiento = establecimientoOptional.get();

                establecimiento.anadirComentario(comentario);
                repoEst.save(establecimiento);

                // Devolver el establecimiento con el comentario agregado
                return ResponseEntity.ok(new EstablecimientoDTO(establecimiento));
            } else {
                return ResponseEntity.notFound().build();
            }
        }


}

