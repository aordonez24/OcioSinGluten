package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Integer> {
    Optional<Actividad> findByAutorAndEstablecimiento(Usuario autor, Establecimiento establecimiento);
    Actividad save(Actividad act);
    void removeActividadByAutorAndEstablecimiento(Usuario autor, Establecimiento establecimiento);

}