package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Integer> {
    @Transactional
    Optional<Establecimiento> findByIdEstablecimiento(int id);
    @Transactional
    Optional<Establecimiento> findByNombreAndCodPostal(String nombre, int codPostal);

    @Transactional
    Optional<Establecimiento> findByVisitantes(Usuario visitante);

    @Transactional
    Establecimiento save(Establecimiento establecimiento);

    @Transactional
    void removeByIdEstablecimiento(int id);

    @Transactional
    List<Establecimiento> findByNombre(String nombre);

    @Transactional
    default Establecimiento actualizar(Establecimiento est) {
        return save(est);
    }

    @Transactional
    List<Establecimiento> findByNombreContaining(String nombre);



}