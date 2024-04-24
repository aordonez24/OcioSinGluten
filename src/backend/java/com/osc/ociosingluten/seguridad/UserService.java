package com.osc.ociosingluten.seguridad;

import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private static UsuarioRepository repository;

    @Override
    public  UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> usuario  = repository.findByUsername(username);
        Usuario usuario1 = usuario.get();

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(usuario1.getRol().toString()));

        UserDetails usu = new User(usuario1.getUsername(), usuario1.getPassword(), authorityList);

        return usu;
    }
}
