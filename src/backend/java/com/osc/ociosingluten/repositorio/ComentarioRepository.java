package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Comentario;
import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, String> {
    Comentario findByAutor(Usuario autor);
    Comentario save(Comentario comentario);
    void removeComentarioByAutor(Usuario autor);

}
