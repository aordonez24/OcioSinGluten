import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.modelo.Establecimiento;
import com.osc.ociosingluten.modelo.Usuario;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest(classes=com.osc.ociosingluten.app.OcioSinGlutenApplication.class)
@ActiveProfiles(profiles = {"test"})
public class ServicioTest {

    private static final String CARACTERES_LETRAS_DNI = "ABCDEFGHJKLMNPQRSTVWXYZ";
    public static String generarDNIAleatorio() {
        Random random = new Random();
        StringBuilder dniBuilder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            dniBuilder.append(random.nextInt(10));
        }
        char letraDNI = CARACTERES_LETRAS_DNI.charAt(random.nextInt(CARACTERES_LETRAS_DNI.length()));
        dniBuilder.append(letraDNI);

        return dniBuilder.toString();
    }

    public String generarCorreoAleatorio() {
        String caracteres = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder correoBuilder = new StringBuilder();
        Random random = new Random();

        int longitudCorreo = 10 + random.nextInt(10); // Longitud entre 10 y 19 caracteres

        for (int i = 0; i < longitudCorreo; i++) {
            correoBuilder.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        correoBuilder.append("@example.com");

        return correoBuilder.toString();
    }

    @Autowired
    ServicioOcioSinGluten servicio;

    @Test
    public void testAccesoServicio(){
        Assertions.assertThat(servicio).isNotNull();
    }

    @BeforeAll
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
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");
        servicio.registroUsuario(usuario);
        Usuario usuarioIncorrecto = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Assertions.assertThatThrownBy(() -> {
                    servicio.registroUsuario(usuarioIncorrecto);
                })
                .isInstanceOf(UsuarioExisteException.class);

        //Segundo caso --> Usuario correcto
        Usuario UsuarioCorrecto = new Usuario(generarDNIAleatorio(), "aor00040", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), "alonsismoF13");

        Assert.assertTrue(servicio.registroUsuario(UsuarioCorrecto));
    }

    @Test
    public void pruebaLoginUsuario() throws UsuarioNoExisteException, ContrasenaIncorrectaException, SesionNoIniciadaException {

        //Primer caso -> Credenciales correctas
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Usuario usuPrueba = servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());
        Assert.assertTrue(usuPrueba.isSesionIniciada());

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

    @Test
    public void pruebaLogOut() throws UsuarioNoExisteException, ContrasenaIncorrectaException, SesionNoIniciadaException, UsuarioExisteException {
        //Primer caso --> Usuario con sesion iniciada puede cerrar sesión
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Usuario usuPrueba = servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());

        Assert.assertTrue(usuPrueba.isSesionIniciada());
        Assert.assertFalse(usuPrueba.isSesionCerrada());

        //Ahora que está la sesión iniciada, debemos cerrarla
        Usuario usu = servicio.logoutUsuario(usuario.getEmail(), usuario.getPassword());
        Assert.assertTrue(usu.isSesionCerrada());
        Assert.assertFalse(usu.isSesionIniciada());


    }

    @Test
    public void testOlvidarContrasena() throws UsuarioNoExisteException, SesionNoIniciadaException, ContrasenaIncorrectaException, UsuarioExisteException {
        //Primer caso, usuario iniciado sesion
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario(generarDNIAleatorio(), "prueba", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), "alonsismoF13");
        servicio.registroUsuario(usuario);
        servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());
        String oldPassword = usuario.getPassword();
        servicio.olvidarContrasena(usuario);
        Assert.assertNotEquals(oldPassword, usuario.getPassword());

        //Segundo caso, usuario sin sesion iniciada
        Usuario usuario2 = new Usuario(generarDNIAleatorio(), "prueba2", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), "alonsismoF13");
        servicio.registroUsuario(usuario2);
        Assertions.assertThatThrownBy(() -> {
                    servicio.olvidarContrasena(usuario);
                })
                .isInstanceOf(SesionNoIniciadaException.class);
    }

    @Test
    public void testBuscarUsuario() throws UsuarioNoExisteException {
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        Usuario usuarios = servicio.buscarUsuario(usuario);

        Assert.assertNotNull(usuarios);

        Usuario usuario1 = new Usuario(generarDNIAleatorio(), "akufhaku2987", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), "alonsismoF13");

        Assertions.assertThatThrownBy(() -> {
                    servicio.buscarUsuario(usuario1);
                })
                .isInstanceOf(UsuarioNoExisteException.class);

    }

    @Test
    public void AnadirEditarEliminarListarEstablecimiento() throws UsuarioNoExisteException, ContrasenaIncorrectaException, EstablecimientoExistenteException, SesionNoIniciadaException, ActividadNoCreada, UsuarioExisteException, NoPermisosException, EstablecimientoNoExistenteException {
        Establecimiento establecimiento = new Establecimiento("Krusty Burger", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "España");
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", "alonsismoF13");

        //Caso 1 --> anadir correctamente el establecimiento
        Usuario usuario1 = servicio.loginUsuario(usuario.getEmail(), usuario.getPassword());
        Assert.assertTrue(servicio.publicarEstablecimiento(usuario1, establecimiento));

        //Caso 2 --> Añadir el mismo establecimiento
        Assertions.assertThatThrownBy(() -> {
                    servicio.publicarEstablecimiento(usuario1, establecimiento);
                })
                .isInstanceOf(EstablecimientoExistenteException.class);

        //Caso 3 --> el usuario no ha iniciado sesión
        Usuario usuario2 = servicio.logoutUsuario(usuario1.getEmail(), usuario.getPassword());
        Assertions.assertThatThrownBy(() -> {
                    servicio.publicarEstablecimiento(usuario2, establecimiento);
                })
                .isInstanceOf(SesionNoIniciadaException.class);

        //Eliminamos el establecimiento, pero primero la actividad
        Usuario admin = new Usuario("78162641Q", "aor00043", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@admin.com", "alonsismoF13");
        servicio.registroUsuario(admin);

        Usuario usuario3 = servicio.loginUsuario(admin.getEmail(), admin.getPassword());
        //Editamos establecimiento
        Assert.assertTrue(servicio.editarEstablecimiento(usuario3, establecimiento, "Krusty Burger", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "Alemania"));

        //Listamos los establecimientos
        List<Establecimiento> establecimientos = servicio.buscarEstablecimiento(establecimiento);

        Assert.assertFalse(establecimientos.isEmpty());

        //Eliminamos el establecimiento, pero primero la actividad (En el propio método borramos la actividad)
        Assert.assertTrue(servicio.eliminarEstablecimiento(usuario3, establecimiento));
    }




}
