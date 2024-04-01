import modelo.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.time.LocalDate;

import static org.junit.Assert.*;

@SpringBootTest
public class SistemaTest {

    Sistema sis;
    Image image;
    Usuario usuarioInicial;

    @Before
    public void setUp() {
        sis = new Sistema();
        Image image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuario(usuarioInicial);
    }

    @Test
    public void testIniciarSesion() {

        //iniciar sesión con un usuario válido y una contraseña correcta
        assertTrue("Usuario válido y contraseña correcta", sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword()));
        assertTrue("Sesión iniciada", sis.getUsuariosRegistrados().get(0).isSesionIniciada());

        //iniciar sesión con un usuario válido pero una contraseña incorrecta
        assertFalse("Usuario válido pero contraseña incorrecta", sis.iniciarSesion(usuarioInicial.getEmail(), "contrasenaincorrecta"));
        assertFalse("Sesión no iniciada", sis.getUsuariosRegistrados().get(0).isSesionIniciada());

        //iniciar sesión con un usuario no registrado
        assertFalse("Usuario no registrado", sis.iniciarSesion("correo@noexistente.com", "contrasena"));

        //iniciar sesión con email null
        assertFalse("Email null", sis.iniciarSesion(null, "contrasena"));

        //iniciar sesión con contraseña null
        assertFalse("Contraseña null", sis.iniciarSesion(usuarioInicial.getEmail(), null));

        //iniciar sesión con usuario válido pero sin contraseña
        assertFalse("Usuario válido pero sin contraseña", sis.iniciarSesion(usuarioInicial.getEmail(), null));
    }

    @Test
    public void testCerrarSesion() {
        // Prueba cerrar sesión con un usuario existente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        assertTrue("Cerrando sesión de usuario existente", sis.cerrarSesion(usuario));
        assertFalse("Sesión cerrada", usuario.isSesionIniciada());
        assertTrue("Sesión cerrada correctamente", usuario.isSesionCerrada());

        // Prueba cerrar sesión con un usuario que no existe
        Usuario usuarioNoExistente = new Usuario("noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        assertFalse("Cerrando sesión de usuario no existente", sis.cerrarSesion(usuarioNoExistente));
    }

    @Test
    public void testOlvidarContraseña() {
        // Prueba para verificar si la contraseña se cambia correctamente cuando la sesión está iniciada
        Usuario usuarioConSesion = sis.getUsuariosRegistrados().get(0);
        usuarioConSesion.setSesionIniciada(true);
        String contrasenaAntesConSesion = usuarioConSesion.getPassword();
        sis.olvidarContraseña(usuarioConSesion);
        assertNotEquals("Contraseña cambiada", contrasenaAntesConSesion, usuarioConSesion.getPassword());

        // Prueba para verificar que la contraseña no se cambia cuando la sesión no está iniciada
        Usuario usuarioSinSesion = sis.getUsuariosRegistrados().get(0);
        usuarioSinSesion.setSesionIniciada(false);
        String contrasenaAntesSinSesion = usuarioSinSesion.getPassword();
        sis.olvidarContraseña(usuarioSinSesion);
        assertEquals("Contraseña no cambiada", contrasenaAntesSinSesion, usuarioSinSesion.getPassword());
    }

    @Test
    public void testRegistro() {
        // Prueba registrar un nuevo usuario con datos válidos
        assertTrue("Registro de nuevo usuario válido", sis.registro("nuevousuario", "Nuevo", "Usuario", LocalDate.now(), 123456789, null, "nuevo@example.com", "contrasena"));
        assertTrue("Nuevo usuario registrado", existeUsuario("nuevo@example.com"));

        // Prueba registrar un usuario con un email ya registrado
        assertFalse("Registro con email ya registrado", sis.registro("otrousuario", "Otro", "Usuario", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));

        // Prueba registrar un usuario con un username ya registrado
        assertFalse("Registro con username ya registrado", sis.registro("aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "otro@example.com", "contrasena"));

        // Prueba registrar un usuario con email y username ya registrados
        assertFalse("Registro con email y username ya registrados", sis.registro("aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));
    }

    private boolean existeUsuario(String email) {
        for (Usuario usuario : sis.getUsuariosRegistrados()) {
            if (usuario.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

}
