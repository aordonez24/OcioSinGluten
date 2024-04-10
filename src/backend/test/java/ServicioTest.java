import com.osc.ociosingluten.excepciones.ContrasenaIncorrectaException;
import com.osc.ociosingluten.excepciones.UsuarioExisteException;
import com.osc.ociosingluten.excepciones.UsuarioNoExisteException;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.AssertTrue;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes=com.osc.ociosingluten.app.OcioSinGlutenApplication.class)
@ActiveProfiles(profiles = {"test"})
public class ServicioTest {

    @Autowired
    ServicioOcioSinGluten servicio;

    @Test
    public void testAccesoServicio(){
        Assertions.assertThat(servicio).isNotNull();
    }

    @BeforeEach
    public void cargarDatosPrueba() throws UsuarioExisteException {
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        servicio.registroUsuario(usuario);
    }

    @Test
    public void pruebaAnadirUsuarioCorrectoEincorrecto() throws UsuarioExisteException {

        //Primer caso --> Usuario incorrecto
        byte[] fotoPerfil = null;
        Usuario usuarioIncorrecto = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Assertions.assertThatThrownBy(() -> {
                    servicio.registroUsuario(usuarioIncorrecto);
                })
                .isInstanceOf(UsuarioExisteException.class);

        //Segundo caso --> Usuario correcto
        Usuario UsuarioCorrecto = new Usuario("78162641Q", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00049@gmail.com", "alonsismoF13");

        Assert.assertTrue(servicio.registroUsuario(UsuarioCorrecto));
    }

    @Test
    public void pruebaLoginUsuario() throws UsuarioNoExisteException, ContrasenaIncorrectaException {

        //Primer caso -> Credenciales correctas
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Assert.assertTrue(servicio.loginUsuario(usuario.getEmail(), usuario.getPassword()));

        //Segundo caso --> Email no existente
        usuario.setEmail("aor00039@red.ujaen.es");
        Assertions.assertThatThrownBy(() -> {
                    servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());
                })
                .isInstanceOf(UsuarioNoExisteException.class);

        //Tercer caso --> Email existente pero contraseña incorrecta
        usuario.setEmail("aor00039@gmail.com");
        usuario.setPassword("alonsismoF133");
        Assertions.assertThatThrownBy(() -> {
                    servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());
                })
                .isInstanceOf(ContrasenaIncorrectaException.class);

    }

    @Test
    public void convertirImagenAByte_ImagenExistente() throws IOException {
        // Directorio de la imagen de prueba
        InputStream inputStream = getClass().getResourceAsStream("/com/osc/ociosingluten/imagenPrueba/_8c971745-c451-48f5-8bf7-05017ae6975e.jpeg");


        // Verificar la imagen facilitada
        byte[] imagenFacilitada = servicio.convertirImagenAByte(inputStream); // Ruta a la imagen facilitada
        assertNotNull(imagenFacilitada, "La imagen facilitada no es nula");
        assertTrue(imagenFacilitada.length > 0, "La imagen facilitada contiene datos");
        assertArrayEquals(imagenFacilitada, imagenFacilitada, "Las imágenes son iguales");

        // Verificar la carga de imagen desde bytes
        BufferedImage imagenDesdeBytes = servicio.cargarImagenDesdeBytes(imagenFacilitada);
        assertNotNull(imagenDesdeBytes, "La imagen cargada desde bytes no es nula");
        assertEquals(1024, imagenDesdeBytes.getWidth(), "El ancho de la imagen cargada es correcto");
        assertEquals(1024, imagenDesdeBytes.getHeight(), "El alto de la imagen cargada es correcto");
    }
}
