package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Actividad;
import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    @Transactional
    Optional<Comentario> findByAutor(Usuario autor);

    @Transactional
    Optional<Comentario> findById(int id);

    @Transactional
    Comentario save(Comentario comentario);
    @Transactional
    void removeComentarioById(Integer id);

    @Transactional
    void delete(Comentario comentario);

    @Transactional
    default Comentario actualizar(Comentario comentario) {
        return save(comentario);
    }




}
