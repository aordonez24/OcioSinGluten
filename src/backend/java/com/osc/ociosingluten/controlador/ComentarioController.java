package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.ComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.NuevoComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.QuitarComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.ComentarioNoExiste;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ComentarioRepository;
import com.osc.ociosingluten.repositorio.EstablecimientoRepository;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ociosingluten/comentario")
@CrossOrigin("http://localhost:3000")
public class ComentarioController {

    @Autowired
    private ComentarioRepository comRe;

    @Autowired
    private ServicioOcioSinGluten servicioOcioSinGluten;

    @Autowired
    private UsuarioRepository repoUsu;

    @Autowired
    private EstablecimientoRepository repoEst;

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioDTO> mostrarUsuarioporDni(@PathVariable int id){
        Optional<Comentario> comentario = comRe.findById(id);
        return comentario.map(c -> ResponseEntity.ok(new ComentarioDTO(c))).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarComentario(@PathVariable int id, @RequestParam String username, @RequestParam int idEstablecimiento) throws ComentarioNoExiste {
        Optional<Comentario> comentarioOptional = comRe.findById(id);
        Optional<Establecimiento> est = repoEst.findByIdEstablecimiento(idEstablecimiento);
        Optional<Usuario> usu = repoUsu.findByUsername(username);

        if (comentarioOptional.isPresent() && est.isPresent() && usu.isPresent()) {
            Comentario com = comentarioOptional.get();
            est.get().eliminarComentario(com);
            repoEst.actualizar(est.get());
            usu.get().eliminarComentario(com);
            repoUsu.actualizarUsuario(usu.get());
            comRe.delete(com);

            return ResponseEntity.ok("Eliminado con éxito.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }



}
