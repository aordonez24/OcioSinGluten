package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Integer> {
    Optional<Establecimiento> findByIdEstablecimiento(int id);
    Optional<Establecimiento> findByNombre(String nombre);
    Establecimiento save(Establecimiento establecimiento);
    void removeEstablecimientoByIdEstablecimiento(int id);

}