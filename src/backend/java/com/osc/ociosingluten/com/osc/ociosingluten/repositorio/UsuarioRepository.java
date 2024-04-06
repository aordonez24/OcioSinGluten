package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Usuario findByEmail(String email);
    Usuario findByUsername(String username);
    Usuario save(Usuario usuario);

    void removeUsuarioByDni(String dni);

}
