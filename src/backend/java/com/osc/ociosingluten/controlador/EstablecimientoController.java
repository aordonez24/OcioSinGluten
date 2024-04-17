package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.EstablecimientoDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.EstablecimientoRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/ociosingluten/establecimientos")
@CrossOrigin("http://localhost:3000")
public class EstablecimientoController {

    @Autowired
    private EstablecimientoRepository repoEst;

    @Autowired
    private ServicioOcioSinGluten servicio;

    //Obtener todos los usuarios que se encuentran registrados en la web, esto solo lo podría ver un usuario admin
    @GetMapping("/listadoEstablecimientos")
    public List<Establecimiento> cargarTodosLosEstablecimientos(){
        return repoEst.findAll();
    }

    @GetMapping("/establecimientos/{nombre}")
    public List<Establecimiento> cargarEstxNombre(@PathVariable String nombre){
        return repoEst.findByNombreContaining(nombre);
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

