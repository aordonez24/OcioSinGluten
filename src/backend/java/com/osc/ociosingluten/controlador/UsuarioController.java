package com.osc.ociosingluten.controlador;

import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ociosingluten")
@CrossOrigin("http://localhost:3000")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repoUsu;

    @GetMapping("/usuarios")
    public List<Usuario> cargarTodosUsuarios(){
        return repoUsu.findAll();
    }
}
