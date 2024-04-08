package com.osc.ociosingluten.repositorio;

import com.osc.ociosingluten.modelo.Usuario;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    @Cacheable("usuarios")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    Optional<Usuario> findByEmail(String email);

    @Cacheable("usuarios")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    Optional<Usuario> findByUsername(String username);

    @Cacheable("usuarios")
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    Optional<Usuario> findByDni(String dni);

    @CacheEvict(value = "usuarios", key="usuario.dni")
    Usuario save(Usuario usuario);

    @CacheEvict(value = "usuarios", key="usuario.dni")
    void removeUsuarioByDni(String dni);

}
