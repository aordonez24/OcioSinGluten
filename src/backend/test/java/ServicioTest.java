import com.osc.ociosingluten.excepciones.*;
import com.osc.ociosingluten.herramientas.Rol;
import com.osc.ociosingluten.modelo.Comentario;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    String contrasena = "alonsismoF13";

    @Test
    public void testAccesoServicio(){
        Assertions.assertThat(servicio).isNotNull();
    }

    @BeforeEach
    public void cargarDatosPrueba() throws UsuarioExisteException {
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);
        servicio.registroUsuario(usuario);

    }

    @Test //Al principio da fallo pero hay que volver a ejecutarlo
    public void pruebaAnadirUsuarioCorrectoEincorrecto() throws UsuarioExisteException {

        //Primer caso --> Usuario incorrecto
        byte[] fotoPerfil = null;

        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Assertions.assertThatThrownBy(() -> {
                    servicio.registroUsuario(usuario);
                })
                .isInstanceOf(UsuarioExisteException.class);

        //Segundo caso --> Usuario correcto
        Usuario UsuarioCorrecto = new Usuario(generarDNIAleatorio(), "aor0000", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);

        Assert.assertTrue(servicio.registroUsuario(UsuarioCorrecto));
    }

    @Test
    public void pruebaLoginUsuario() throws UsuarioNoExisteException, ContrasenaIncorrectaException, SesionNoIniciadaException {

        //Primer caso -> Credenciales correctas
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Usuario usuPrueba = servicio.loginUsuario(usuario.getEmail(), contrasena);
        Assert.assertTrue(usuPrueba.isSesionIniciada());

        //Segundo caso --> Email no existente
        Assertions.assertThatThrownBy(() -> {
                    servicio.loginUsuario("aor00066@red.ujaen.es", contrasena);
                })
                .isInstanceOf(UsuarioNoExisteException.class);

        //Tercer caso --> Email existente pero contraseña incorrecta
        usuario.setEmail("aor00039@gmail.com");
        Assertions.assertThatThrownBy(() -> {
                    servicio.loginUsuario(usuario.getEmail(), "kjasdfsdfa");
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
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Usuario usuPrueba = servicio.loginUsuario(usuario.getEmail(), contrasena);

        Assert.assertTrue(usuPrueba.isSesionIniciada());
        Assert.assertFalse(usuPrueba.isSesionCerrada());

        //Ahora que está la sesión iniciada, debemos cerrarla
        Usuario usu = servicio.logoutUsuario(usuario.getEmail(), contrasena);
        Assert.assertTrue(usu.isSesionCerrada());
        Assert.assertFalse(usu.isSesionIniciada());


    }

    @Test
    public void testOlvidarContrasena() throws UsuarioNoExisteException, SesionNoIniciadaException, ContrasenaIncorrectaException, UsuarioExisteException {
        //Primer caso, usuario iniciado sesion
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario(generarDNIAleatorio(), "prueba", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);
        servicio.registroUsuario(usuario);
        Usuario usuar1 = servicio.loginUsuario(usuario.getEmail(), contrasena);
        String oldPassword = usuario.getPassword();
        servicio.olvidarContrasena(usuar1);
        Assert.assertNotEquals(contrasena, usuario.getPassword());

    }

    @Test
    public void testBuscarUsuario() throws UsuarioNoExisteException {
        byte[] fotoPerfil = null;
        Usuario usuario = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Usuario usuarios = servicio.buscarUsuario(usuario);

        Assert.assertNotNull(usuarios);

        Usuario usuario1 = new Usuario(generarDNIAleatorio(), "akufhaku2987", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);

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
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        //Caso 1 --> anadir correctamente el establecimiento
        Usuario usuario1 = servicio.loginUsuario(usuario.getEmail(), contrasena);
        Assert.assertTrue(servicio.publicarEstablecimiento(usuario1, establecimiento));

        //Caso 2 --> Añadir el mismo establecimiento
        Assertions.assertThatThrownBy(() -> {
                    servicio.publicarEstablecimiento(usuario1, establecimiento);
                })
                .isInstanceOf(EstablecimientoExistenteException.class);

        //Caso 3 --> el usuario no ha iniciado sesión
        Usuario usuario2 = servicio.logoutUsuario(usuario1.getEmail(), contrasena);
        Assertions.assertThatThrownBy(() -> {
                    servicio.publicarEstablecimiento(usuario2, establecimiento);
                })
                .isInstanceOf(SesionNoIniciadaException.class);

        //Eliminamos el establecimiento, pero primero la actividad
        Usuario admin = new Usuario("78162641Q", "aor00043", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@admin.com", contrasena);
        servicio.registroUsuario(admin);

        Usuario usuario3 = servicio.loginUsuario(admin.getEmail(), contrasena);
        //Editamos establecimiento
        Assert.assertTrue(servicio.editarEstablecimiento(usuario3, establecimiento, "Krusty Burger", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "Alemania"));

        //Listamos los establecimientos
        List<Establecimiento> establecimientos = servicio.buscarEstablecimiento(establecimiento);

        Assert.assertFalse(establecimientos.isEmpty());

        //Eliminamos el establecimiento, pero primero la actividad (En el propio método borramos la actividad)
        Assert.assertTrue(servicio.eliminarEstablecimiento(usuario3, establecimiento));
    }

    @Test
    public void pruebaAnadirEditarEliminarComentario() throws EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException, UsuarioNoExisteException, EstablecimientoNoExistenteException, ContrasenaIncorrectaException, ComentarioNoExiste {
        Establecimiento establecimiento = new Establecimiento("La Espuela", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "España");
        byte[] fotoPerfil = null;
        Usuario comentador = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Usuario usu = servicio.loginUsuario(comentador.getEmail(), contrasena);
        Comentario com = new Comentario("Que bien se come hay, además te atienden muy rapido. Un gustazo!", comentador);

        Assert.assertTrue(servicio.publicarEstablecimiento(usu, establecimiento));
        Assert.assertTrue(servicio.comentarEstablecimiento(usu, establecimiento, com));

        String nuevoMensaje = "Que bien se come ahí, además te atienden muy rapido. Un gustazo!";
        Assert.assertTrue(servicio.editarComentario(usu, com, nuevoMensaje));



        Assert.assertTrue(servicio.eliminarComentarioEstablecimiento(usu, com, establecimiento));

    }


    @Test
    public void visitarEstablecimiento() throws UsuarioNoExisteException, ContrasenaIncorrectaException, EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException, EstablecimientoNoExistenteException, UsuarioExisteException {
        Establecimiento establecimiento = new Establecimiento("Burger King", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "España");
        byte[] fotoPerfil = null;
        Usuario comentador = new Usuario(generarDNIAleatorio(), "aor00050", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);
        servicio.registroUsuario(comentador);
        Usuario usu = servicio.loginUsuario(comentador.getEmail(), contrasena);
        Assert.assertTrue(servicio.publicarEstablecimiento(usu, establecimiento));
        Assert.assertTrue(servicio.visitarEstablecimiento(usu, establecimiento));
    }

    @Test
    public void favEstablecimiento() throws UsuarioNoExisteException, ContrasenaIncorrectaException, EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException, EstablecimientoNoExistenteException, UsuarioExisteException {
        Establecimiento establecimiento = new Establecimiento("McDonalds", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "España");
        byte[] fotoPerfil = null;
        Usuario comentador = new Usuario(generarDNIAleatorio(), "aor00051", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);
        servicio.registroUsuario(comentador);
        Usuario usu = servicio.loginUsuario(comentador.getEmail(), contrasena);
        Assert.assertTrue(servicio.publicarEstablecimiento(usu, establecimiento));
        Assert.assertTrue(servicio.visitarEstablecimiento(usu, establecimiento));
        Assert.assertTrue(servicio.marcarEstablecimientoFavorito(usu, establecimiento));

        Assert.assertTrue(servicio.darLikeEstablecimiento(usu, establecimiento));
    }

    @Test
    public void seguirUsuario() throws UsuarioExisteException, UsuarioNoExisteException, SesionNoIniciadaException, ContrasenaIncorrectaException {
        byte[] fotoPerfil = null;
        Usuario elQueSigue = new Usuario(generarDNIAleatorio(), "aor00777", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);

        Usuario elSeguido = new Usuario(generarDNIAleatorio(), "aor00776", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);

        servicio.registroUsuario(elQueSigue);
        servicio.registroUsuario(elSeguido);

        Usuario usu = servicio.loginUsuario(elQueSigue.getEmail(), contrasena);
        Assert.assertTrue(servicio.seguirUsuario(usu, elSeguido));

    }

    @Test
    public void comentarComentario() throws UsuarioNoExisteException, ContrasenaIncorrectaException, EstablecimientoExistenteException, ActividadNoCreada, SesionNoIniciadaException, EstablecimientoNoExistenteException, UsuarioExisteException, ComentarioNoExiste {
        Establecimiento establecimiento = new Establecimiento("Taberna Don Sancho", 620979747, "Jaén", "Jaén", "Avenida de Andalucía", 23006, "España");
        byte[] fotoPerfil = null;
        Usuario comentador = new Usuario("78162640S", "aor00039", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, "aor00039@gmail.com", contrasena);

        Usuario usu = servicio.loginUsuario(comentador.getEmail(), contrasena);
        Comentario com = new Comentario("Que bien se come ahí, además te atienden muy rapido. Un gustazo!", comentador);

        Assert.assertTrue(servicio.publicarEstablecimiento(usu, establecimiento));
        Assert.assertTrue(servicio.comentarEstablecimiento(usu, establecimiento, com));

        Usuario comentador2 = new Usuario(generarDNIAleatorio(), "aor00770", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);
        servicio.registroUsuario(comentador2);
        Usuario usu2 = servicio.loginUsuario(comentador2.getEmail(), contrasena);

        Comentario respuesta = new Comentario("Si, la verdad es que sí, tienen muchísima variedad!", comentador2);

        Assert.assertTrue(servicio.comentarComentario(usu2, com, respuesta, 1, ""));


    }

    @Test
    public void testGestionUsuario() throws UsuarioExisteException, UsuarioNoExisteException, ContrasenaIncorrectaException, NoPermisosException, SesionNoIniciadaException {
        byte[] fotoPerfil = null;
        Usuario gestionado = new Usuario(generarDNIAleatorio(), "aor10000", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24)
                ,670988953, fotoPerfil, generarCorreoAleatorio(), contrasena);

        servicio.registroUsuario(gestionado);
        String nuevoNombre = "Alvarín";
        Usuario gets = servicio.loginUsuario(gestionado.getEmail(), contrasena);
        servicio.gestionUsuario(gets, 2, gets, nuevoNombre, gestionado.getApellidos(), gestionado.getEmail(), contrasena, gestionado.getFechaNacimiento());
        Assert.assertEquals(nuevoNombre, gets.getNombre());

    }




}
