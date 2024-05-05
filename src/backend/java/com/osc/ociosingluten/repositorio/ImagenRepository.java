package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.herramientas.MensajePredefinido;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Imagen;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Integer> {

    @Transactional
    Imagen save(Imagen img);

    @Transactional
    Imagen findByIdImagen(Imagen img);
}




