package com.osc.ociosingluten.controlador;


import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.EstablecimientoRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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



}

