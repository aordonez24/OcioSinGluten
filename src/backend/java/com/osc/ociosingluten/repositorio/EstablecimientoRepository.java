package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Establecimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, String> {
    Establecimiento findByIdEstablecimiento(int id);
    Establecimiento findByNombre(String nombre);
    Establecimiento save(Establecimiento establecimiento);
    void removeEstablecimientoByIdEstablecimiento(int id);

}