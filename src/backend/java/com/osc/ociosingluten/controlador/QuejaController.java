package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.controlador.DTO.QuejaDTO;
import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Queja;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.QuejaRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
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
    public List<Queja> cargarTodasQuejas(){
        return repoAct.findAll();
    }

    @PostMapping("/nuevaQueja")
    public ResponseEntity<QuejaDTO> anadirUsuario(@RequestBody QuejaDTO quejaDTO) throws IOException, UsuarioExisteException {
        Queja q = new Queja(quejaDTO.nombre(), quejaDTO.email(), quejaDTO.mensaje());
        repoAct.save(q);
        QuejaDTO qe = new QuejaDTO(q);
        return ResponseEntity.status(HttpStatus.CREATED).body(qe);
    }


}
