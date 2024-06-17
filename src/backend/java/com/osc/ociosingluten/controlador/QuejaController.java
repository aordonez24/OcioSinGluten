package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.QuejaDTO;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.modelo.Consulta;
import com.osc.ociosingluten.repositorio.QuejaRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/ociosingluten/quejas")
@CrossOrigin("http://localhost:3000")
public class QuejaController {

    @Autowired
    private QuejaRepository repoAct;

    @Autowired
    private ServicioOcioSinGluten servicio;

    //Obtener todos los usuarios que se encuentran registrados en la web, esto solo lo podría ver un usuario admin
    @GetMapping("/listadoQuejas")
    public List<Consulta> cargarTodasQuejas(){
        return repoAct.findAll();
    }

    @PostMapping("/nuevaQueja")
    public ResponseEntity<QuejaDTO> anadirConsulta(@RequestBody QuejaDTO quejaDTO) throws IOException, UsuarioExisteException {
        Consulta q = new Consulta(quejaDTO.nombre(), quejaDTO.email(), quejaDTO.mensaje());
        repoAct.save(q);
        return ResponseEntity.status(HttpStatus.CREATED).body(new QuejaDTO(q));
    }

    @DeleteMapping("/quitaQueja/{id}")
    public ResponseEntity<Void> eliminarQueja(@PathVariable int id) {
        repoAct.deleteByIdQueja(id);
        return ResponseEntity.noContent().build();
    }


}
