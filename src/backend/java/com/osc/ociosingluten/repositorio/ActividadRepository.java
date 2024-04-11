package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.herramientas.MensajePredefinido;
import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {

    @Transactional
    Optional<Actividad> findByEstablecimiento(Establecimiento establecimiento);

    @Transactional
    Actividad save(Actividad act);

    @Transactional
    void removeActividadByAutorAndEstablecimiento(Usuario autor, Establecimiento establecimiento);

    @Transactional
    List<Actividad> findAllByAutor(Usuario autor);

    @Transactional
    void delete(Actividad actividad);




}