package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ociosingluten/actividades")
@CrossOrigin("http://localhost:3000")
public class ActividadController {

    @Autowired
    private ActividadRepository repoAct;

    @GetMapping("/listadoActividades")
    public List<Actividad> cargarTodasActividades(){
        return repoAct.findAll();
    }


}
