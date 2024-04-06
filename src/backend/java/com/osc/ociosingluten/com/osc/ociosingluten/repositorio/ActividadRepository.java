package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActividadRepository extends JpaRepository<Actividad, String> {
    Actividad findByAutorAndEstablecimiento(Usuario autor, Establecimiento establecimiento);
    Actividad save(Actividad act);
    void removeActividadByAutorAndEstablecimiento(Usuario autor, Establecimiento establecimiento);

}