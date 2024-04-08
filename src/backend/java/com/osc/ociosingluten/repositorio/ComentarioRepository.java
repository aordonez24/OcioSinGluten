package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComentarioRepository extends JpaRepository<Comentario, String> {
    Optional<Comentario> findByAutor(Usuario autor);
    Comentario save(Comentario comentario);
    void removeComentarioByAutor(Usuario autor);

}
