package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.controlador.DTO.ComentarioDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ComentarioRepository;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
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
    private UsuarioRepository repoUsu;

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioDTO> mostrarUsuarioporDni(@PathVariable int id){
        Optional<Comentario> comentario = comRe.findById(id);
        return comentario.map(c -> ResponseEntity.ok(new ComentarioDTO(c))).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/respuestas")
    public ResponseEntity<List<ComentarioDTO>> obtenerRespuestas(@PathVariable int id){
        Optional<Comentario> comentarioOptional = comRe.findById(id);

        if (comentarioOptional.isPresent()) {
            Comentario comentario = comentarioOptional.get();
            List<Comentario> respuestas = comentario.getComentarios();

            List<ComentarioDTO> respuestasDTO = respuestas.stream()
                    .map(ComentarioDTO::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(respuestasDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/respuestas")
    public ResponseEntity<ComentarioDTO> agregarRespuesta(@PathVariable int id, @RequestBody ComentarioDTO respuestaDTO) {
        Optional<Comentario> comentarioOptional = comRe.findById(id);

        if (comentarioOptional.isPresent()) {
            Comentario comentarioPadre = comentarioOptional.get();
            Optional<Usuario> usuario = repoUsu.findByUsername(respuestaDTO.nombreAutor());
            Comentario respuesta = new Comentario(respuestaDTO.mensaje(), usuario.get());
            respuesta.setComentarioPadre(comentarioPadre);
            Comentario respuestaGuardada = comRe.save(respuesta);
            comentarioPadre.anadirComentario(respuestaGuardada);
            comRe.save(comentarioPadre);

            return ResponseEntity.ok(new ComentarioDTO(respuestaGuardada));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}
