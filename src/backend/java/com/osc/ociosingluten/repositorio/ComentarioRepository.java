package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    Optional<Comentario> findByAutor(Usuario autor);
    Comentario save(Comentario comentario);
    void removeComentarioByAutor(Usuario autor);

}
