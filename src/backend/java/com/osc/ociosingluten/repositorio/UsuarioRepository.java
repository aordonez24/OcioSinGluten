package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByDni(String dni);
    Usuario save(Usuario usuario);
    void removeUsuarioByDni(String dni);

}
