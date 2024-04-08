package com.osc.ociosingluten.servicio;

import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.ActividadRepository;
import com.osc.ociosingluten.repositorio.ComentarioRepository;
import com.osc.ociosingluten.repositorio.EstablecimientoRepository;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class ServicioOcioSinGluten {

    @Autowired
    UsuarioRepository repoUsuario;

    @Autowired
    EstablecimientoRepository repoEst;

    @Autowired
    ComentarioRepository repoComen;

    @Autowired
    ActividadRepository repoAct;

    @Autowired
    CacheManager cacheManager;

    public ServicioOcioSinGluten() {
    }

    public Optional<Usuario> registroUsuario(@NotNull @Valid Usuario usu) throws UsuarioExisteException {
        Optional<Usuario> prueba = repoUsuario.findByDni(usu.getDni());
        if(prueba.isPresent())
            throw new UsuarioExisteException("El usuario ya existe.");
        return prueba;
    }
}
