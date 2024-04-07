package com.osc.ociosingluten;

import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.repositorio.UsuarioRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repositorio;

    @Test
    public void estOperacionesUsuario(){
        String email = "aor00039@red.ujaen.es";
        LocalDate fecha = LocalDate.of(2002, 10, 24);
        byte[] foto = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, foto, email, "holisimaF13");

        repositorio.save(usuario);

        Usuario usuarioGuardado = repositorio.findByEmail(email);
        Usuario usuario1 = repositorio.findByUsername("aor00039");

        Assert.assertNotNull(usuarioGuardado);
        Assert.assertEquals("Alvaro",usuarioGuardado.getNombre());
        Assert.assertEquals("78162640S",usuarioGuardado.getDni());
        Assert.assertEquals("78162640S",usuario1.getDni());


        String dni = usuario.getDni();
        repositorio.removeUsuarioByDni(dni);

        Usuario usuarioBorrado = repositorio.findByEmail(email);
        Assert.assertNull(usuarioBorrado);

    }
}
