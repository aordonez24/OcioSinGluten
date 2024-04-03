import excepciones.*;
import herramientas.Direccion;
import modelo.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import static org.junit.Assert.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertFalse;

@SpringBootTest
public class SistemaTest {

    Sistema sis;
    Usuario usuarioInicial;
    Usuario usuarioAdmin;

    @Before
    public void setUp() {
        sis = new Sistema();
        byte[] image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Direccion dir = new Direccion("Jaén", "Jaén", "Avenida de Andalucía, 83", 23006, "España");
        Establecimiento establecimiento1 = new Establecimiento("EstPrueba1", 670988953, dir);
        sis.anadirEstablecimientoPrueba(establecimiento1);
        usuarioInicial.setSesionIniciada(true);
        Comentario com = new Comentario("aaaaaaaaaaaaaaaaaaaaa", usuarioInicial);
        boolean b = sis.anadirComentario(com.getMensaje(), establecimiento1, usuarioInicial);
        usuarioInicial.setSesionIniciada(false);
        usuarioAdmin = new Usuario("aor00030", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@admin.es", "holaaaaa");
        sis.anadirUsuarioPrueba(usuarioAdmin);
        Actividad act = new Actividad(usuarioInicial, establecimiento1, fecha, MensajePredefinido.HA_VISITADO);
        sis.anadirActividadPrueba(act);
    }

    @Test
    public void testIniciarSesion() throws UsuarioNoExisteException {
        //iniciar sesión con un usuario válido y una contraseña correcta
        assertTrue("Usuario válido y contraseña correcta", sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword()));
        assertTrue("Sesión iniciada", sis.getUsuariosRegistrados().get(0).isSesionIniciada());
        sis.cerrarSesion(sis.getUsuariosRegistrados().get(0));

        //iniciar sesión con un usuario válido pero una contraseña incorrecta
        assertThrows(UsuarioNoExisteException.class, () -> sis.iniciarSesion(usuarioInicial.getEmail(), "contrasenaincorrecta"));
        Assert.assertFalse("Sesión no iniciada", sis.getUsuariosRegistrados().get(0).isSesionIniciada());

        //iniciar sesión con un usuario no registrado
        assertThrows(UsuarioNoExisteException.class, () -> sis.iniciarSesion("correo@noexistente.com", "contrasena"));

        //iniciar sesión con email null
        assertThrows(UsuarioNoExisteException.class, () -> sis.iniciarSesion(null, "contrasena"));

        //iniciar sesión con contraseña null
        assertThrows(UsuarioNoExisteException.class, () -> sis.iniciarSesion(usuarioInicial.getEmail(), null));

        //iniciar sesión con usuario válido pero sin contraseña
        assertThrows(UsuarioNoExisteException.class, () -> sis.iniciarSesion(usuarioInicial.getEmail(), null));
    }

    @Test
    public void testCerrarSesion() {
        //cerrar sesión con un usuario existente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        assertTrue("Cerrando sesión de usuario existente", sis.cerrarSesion(usuario));
        Assert.assertFalse("Sesión cerrada", usuario.isSesionIniciada());
        assertTrue("Sesión cerrada correctamente", usuario.isSesionCerrada());

        //cerrar sesión con un usuario que no existe
        Usuario usuarioNoExistente = new Usuario("noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        Assert.assertFalse("Cerrando sesión de usuario no existente", sis.cerrarSesion(usuarioNoExistente));
    }

    @Test
    public void testOlvidarContraseña() {
        //para verificar si la contraseña se cambia correctamente cuando la sesión está iniciada
        Usuario usuarioConSesion = sis.getUsuariosRegistrados().get(0);
        usuarioConSesion.setSesionIniciada(true);
        String contrasenaAntesConSesion = usuarioConSesion.getPassword();
        sis.olvidarContraseña(usuarioConSesion);
        assertNotEquals("Contraseña cambiada", contrasenaAntesConSesion, usuarioConSesion.getPassword());

        //para verificar que la contraseña no se cambia cuando la sesión no está iniciada
        Usuario usuarioSinSesion = sis.getUsuariosRegistrados().get(0);
        usuarioSinSesion.setSesionIniciada(false);
        String contrasenaAntesSinSesion = usuarioSinSesion.getPassword();
        sis.olvidarContraseña(usuarioSinSesion);
        assertEquals("Contraseña no cambiada", contrasenaAntesSinSesion, usuarioSinSesion.getPassword());
    }

    @Test
    public void testRegistro() {
        //registrar un nuevo usuario con datos válidos
        try {
            assertTrue("Registro de nuevo usuario válido", sis.registro("nuevousuario", "Nuevo", "Usuario", LocalDate.now(), 123456789, null, "nuevo@example.com", "contrasena"));
            assertTrue("Nuevo usuario registrado", existeUsuario("nuevo@example.com"));
        } catch (EmailYaExistenteException | ErrorDatosException e) {
            fail("No se esperaba ninguna excepción aquí");
        }

        //registrar un usuario con un email ya registrado
        try {
            assertFalse("Registro con email ya registrado", sis.registro("otrousuario", "Otro", "Usuario", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (EmailYaExistenteException e) {
        } catch (ErrorDatosException e) {
            fail("Se esperaba EmailYaExistenteException, pero se lanzó ErrorDatosException");
        }

        //registrar un usuario con un username ya registrado
        try {
            assertFalse("Registro con username ya registrado", sis.registro("aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "otro@example.com", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (EmailYaExistenteException e) {
        } catch (ErrorDatosException e) {
            fail("Se esperaba EmailYaExistenteException, pero se lanzó ErrorDatosException");
        }

        //registrar un usuario con email y username ya registrados
        try {
            assertFalse("Registro con email y username ya registrados", sis.registro("aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (EmailYaExistenteException e) {
        } catch (ErrorDatosException e) {
            fail("Se esperaba EmailYaExistenteException, pero se lanzó ErrorDatosException");
        }
    }

    private boolean existeUsuario(String email) {
        for (Usuario usuario : sis.getUsuariosRegistrados()) {
            if (usuario.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testBuscarUsuario() {
        //buscar un usuario con sesión iniciada y que existe
        Usuario usuarioConSesionIniciada = sis.getUsuariosRegistrados().get(0);
        usuarioConSesionIniciada.setSesionIniciada(true);
        assertDoesNotThrow(() -> {
            assertEquals("Buscando usuario con sesión iniciada y que existe", usuarioConSesionIniciada, sis.buscarUsuario(usuarioConSesionIniciada));
        });

        //buscar un usuario con sesión no iniciada
        Usuario usuarioSinSesion = sis.getUsuariosRegistrados().get(0);
        usuarioSinSesion.setSesionIniciada(false);
        assertThrows(SesionNoIniciadaException.class, () -> {
            sis.buscarUsuario(usuarioSinSesion);
        });

        //buscar un usuario que no existe
        Usuario usuarioNoExistente = new Usuario("noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        usuarioNoExistente.setSesionIniciada(true);
        assertThrows(UsuarioNoExisteException.class, () -> {
            sis.buscarUsuario(usuarioNoExistente);
        });
    }

    @Test
    public void testBuscarEstablecimiento() {
        //buscar un establecimiento existente
        Establecimiento establecimientoExistente = sis.getEstablecimientosRegistrados().get(0);
        try {
            assertEquals("Establecimiento existente encontrado", establecimientoExistente, sis.buscarEstablecimiento(establecimientoExistente));
        } catch (EstablecimientoNoExistenteException e) {
            fail("Se lanzó una excepción inesperada: " + e.getMessage());
        }

        Direccion dir = new Direccion();
        //buscar un establecimiento que no existe
        Establecimiento establecimientoNoExistente = new Establecimiento("NombreNoExistente", 345434857, dir);
        try {
            assertNull("Establecimiento no existente no encontrado", sis.buscarEstablecimiento(establecimientoNoExistente));
        } catch (EstablecimientoNoExistenteException e) {
            // Se esperaba que se lanzara esta excepción, no hace falta hacer nada aquí.
        }
    }


    @Test
    public void testAnadirComentario() throws UsuarioNoExisteException {
        //añadir comentario con sesión iniciada y texto válido
        Usuario usuario1 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario1.getEmail(), usuario1.getPassword());
        String texto1 = "Este es un comentario válido";
        assertTrue("Añadiendo comentario con sesión iniciada y texto válido", sis.anadirComentario(texto1, sis.getEstablecimientosRegistrados().get(0), usuario1));

        //añadir comentario con sesión no iniciada
        Usuario usuario2 = sis.getUsuariosRegistrados().get(0);
        sis.cerrarSesion(usuario2);
        String texto2 = "Este comentario no debería añadirse";
        Assert.assertFalse("Intento de añadir comentario con sesión no iniciada", sis.anadirComentario(texto2, sis.getEstablecimientosRegistrados().get(0), usuario2));

        //añadir comentario con texto vacío
        Usuario usuario3 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario3.getEmail(), usuario3.getPassword());
        String texto3 = "";
        Assert.assertFalse("Intento de añadir comentario con texto vacío", sis.anadirComentario(texto3, sis.getEstablecimientosRegistrados().get(0), usuario3));

        //añadir comentario con un texto que excede el límite de caracteres
        Usuario usuario4 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario4.getEmail(), usuario4.getPassword());
        // Crear texto con más de 140 caracteres
        String texto4 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        Assert.assertFalse("Intento de añadir comentario con texto que excede el límite de caracteres", sis.anadirComentario(texto4, sis.getEstablecimientosRegistrados().get(0), usuario4));
    }

    @Test
    public void testEliminarComentario() throws UsuarioNoExisteException {
        //eliminar comentario con un comentario existente y sesión iniciada
        Comentario comentario = sis.getComentariosRealizados().get(0);
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Eliminar comentario existente con sesión iniciada", sis.eliminarComentario(comentario, usuarioInicial));
        Assert.assertFalse("Comentario eliminado", sis.getComentariosRealizados().contains(comentario));

        //eliminar comentario con sesión no iniciada
        Usuario usuarioSinSesion = new Usuario("sinSesion", "Sin", "Sesión", LocalDate.now(), 123456789, null, "sin@sesion.com", "contrasena");
        Assert.assertFalse("Eliminar comentario con sesión no iniciada", sis.eliminarComentario(comentario, usuarioSinSesion));

        //eliminar comentario con comentario no existente
        Comentario comentarioNoExistente = new Comentario("No existe", usuarioInicial);
        Assert.assertFalse("Eliminar comentario no existente", sis.eliminarComentario(comentarioNoExistente, usuarioInicial));
    }

    @Test
    public void testEditarComentario() throws UsuarioNoExisteException {
        //editar comentario con usuario y sesión válidos y texto diferente
        Direccion dir = new Direccion();
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Comentario comentario = sis.getComentariosRealizados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Editando comentario con usuario y sesión válidos y texto diferente", sis.editarComentario(comentario, "Nuevo texto", usuario));
        assertEquals("Texto editado correctamente", "Nuevo texto", comentario.getMensaje());

        //editar comentario con usuario y sesión válidos y texto igual
        Assert.assertFalse("Editando comentario con usuario y sesión válidos y texto igual", sis.editarComentario(comentario, "Nuevo texto", usuario));
        assertEquals("Texto no cambió", "Nuevo texto", comentario.getMensaje());

        //editar comentario con usuario no autorizado
        Usuario usuarioNoAutorizado = new Usuario("noautorizado", "No", "Autorizado", LocalDate.now(), 123456789, null, "noautorizado@example.com", "contrasena");
        Assert.assertFalse("Editando comentario con usuario no autorizado", sis.editarComentario(comentario, "Nuevo texto 2", usuarioNoAutorizado));

        //editar comentario con usuario autorizado pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        Assert.assertFalse("Editando comentario con usuario autorizado pero sin sesión iniciada", sis.editarComentario(comentario, "Nuevo texto 3", usuario));

        //editar comentario con comentario nulo
        Assert.assertFalse("Editando comentario nulo", sis.editarComentario(null, "Nuevo texto 4", usuario));

        //editar comentario con texto nuevo nulo
        Assert.assertFalse("Editando comentario con texto nuevo nulo", sis.editarComentario(comentario, null, usuario));

        //editar comentario con usuario nulo
        Assert.assertFalse("Editando comentario con usuario nulo", sis.editarComentario(comentario, "Nuevo texto 5", null));
    }

    @Test
    public void testAnadirEstablecimiento() throws UsuarioNoExisteException {
        //agregar un establecimiento válido
        Direccion dir = new Direccion();
        dir.setCalle("Avenida De Andalucía Jaén");
        sis.getEstablecimientosRegistrados().clear();
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Añadiendo establecimiento válido", sis.anadirEstablecimiento("Restaurante A", dir, 123456789, usuario));
        assertEquals("Establecimiento añadido correctamente", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con nombre demasiado largo
        Assert.assertFalse("Añadiendo establecimiento con nombre demasiado largo", sis.anadirEstablecimiento("Restaurante con un nombre muy largo que excede el límite de caracteres permitido", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con nombre vacío
        Assert.assertFalse("Añadiendo establecimiento con nombre vacío", sis.anadirEstablecimiento("", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección demasiado larga
        dir.setCalle("hjbfksdabfadskbgksfdjngkjdfngkjfdwngkanglskfjgbsflkdjgnsfkjgndsflkgjnffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffsdflgkjbsdfngkjsdfbngkjsdfnbkñjdnbkesfnbksdfjnbsdfñjsdñjgsdñjsfuhunsdñkjsdñlgfñjldjhnsdfñhndsfgndsfhjsndfñhkjnsdfgunsdfbuesrnhpieurbnkñsdfngs");
        Assert.assertFalse("Añadiendo establecimiento con dirección demasiado larga", sis.anadirEstablecimiento("Restaurante B", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección vacía
        dir.setCalle("");
        Assert.assertFalse("Añadiendo establecimiento con dirección vacía", sis.anadirEstablecimiento("Restaurante B", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con número de teléfono incorrecto
        dir.setCalle("Avenida De Andalucía Jaén");
        Assert.assertFalse("Añadiendo establecimiento con número de teléfono incorrecto", sis.anadirEstablecimiento("Restaurante C", dir, 12345, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con usuario nulo
        dir.setCalle("Avenida De Andalucía Jaén");
        Assert.assertFalse("Añadiendo establecimiento con usuario nulo", sis.anadirEstablecimiento("Restaurante D", dir, 123456789, null));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());
    }

    @Test
    public void testEliminarEstablecimiento() throws UsuarioNoExisteException, EstablecimientoNoExistenteException {
        // Prueba eliminar un establecimiento con usuario admin y sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(1);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Eliminando establecimiento con usuario admin y sesión iniciada", sis.eliminarEstablecimiento(establecimiento, usuario));
        assertEquals("Establecimiento eliminado correctamente", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario no admin
        Usuario usuarioNoAdmin = new Usuario("noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        Assert.assertFalse("Eliminando establecimiento con usuario no admin", sis.eliminarEstablecimiento(establecimiento, usuarioNoAdmin));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario admin pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        Assert.assertFalse("Eliminando establecimiento con usuario admin pero sin sesión iniciada", sis.eliminarEstablecimiento(establecimiento, usuario));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario null
        Assert.assertFalse("Eliminando establecimiento con usuario null", sis.eliminarEstablecimiento(establecimiento, null));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento null
        Assert.assertFalse("Eliminando establecimiento null", sis.eliminarEstablecimiento(null, usuario));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());
    }

    @Test
    public void testEditarEstablecimiento() throws UsuarioNoExisteException, EstablecimientoNoExistenteException {
        //editar un establecimiento con usuario admin y sesión iniciada
        Direccion dir = new Direccion();
        Usuario usuario = sis.getUsuariosRegistrados().get(1);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        dir.setCalle("Avenida Andalucía");
        assertTrue("Editando establecimiento con usuario admin y sesión iniciada", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", dir, 987654321));
        assertEquals("Nombre editado correctamente", "Nuevo nombre", establecimiento.getNombre());
        assertEquals("Teléfono editado correctamente", 987654321, establecimiento.getTelefono());

        //editar un establecimiento con usuario no admin
        Usuario usuarioNoAdmin = new Usuario("noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        Assert.assertFalse("Editando establecimiento con usuario no admin", sis.editarEstablecimiento(establecimiento, usuarioNoAdmin, "Nuevo nombre", dir, 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con usuario admin pero sin sesión iniciada
        sis.cerrarSesion(usuario);
        Assert.assertFalse("Editando establecimiento con usuario admin pero sin sesión iniciada", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", dir, 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con establecimiento null
        Assert.assertFalse("Editando establecimiento null", sis.editarEstablecimiento(null, usuario, "Nuevo nombre", dir, 987654321));

        //editar un establecimiento con usuario null
        Assert.assertFalse("Editando establecimiento con usuario null", sis.editarEstablecimiento(establecimiento, null, "Nuevo nombre", dir, 987654321));
    }

    @Test
    public void testVisitarEstablecimiento() throws UsuarioNoExisteException, EstablecimientoNoExistenteException {
        // Prueba visitar un establecimiento con sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        sis.visitarEstablecimiento(establecimiento, usuario);
        establecimiento.anadirVisitante(usuario);
        assertTrue("Usuario ha visitado el establecimiento", usuario.getEstablecimientosVisitados().contains(establecimiento));
        assertEquals("Establecimiento visitado correctamente", 1, establecimiento.getVisitantes().size());

        // Prueba visitar un establecimiento sin sesión iniciada

        sis.cerrarSesion(usuario);
        usuario.getEstablecimientosVisitados().clear();
        sis.visitarEstablecimiento(establecimiento, usuario);
        Assert.assertFalse("Usuario no ha visitado el establecimiento sin sesión iniciada", usuario.getEstablecimientosVisitados().contains(establecimiento));
    }

    @Test
    public void testMarcarFavoritoEstablecimiento() throws UsuarioNoExisteException, EstablecimientoNoExistenteException {
        // Prueba marcar un establecimiento como favorito con sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        sis.marcarFavoritoEstablecimiento(establecimiento, usuario);
        assertTrue("Usuario ha marcado el establecimiento como favorito", usuario.getEstablecimientosFavoritos().contains(establecimiento));

        // Prueba marcar un establecimiento como favorito sin sesión iniciada
        sis.cerrarSesion(usuario);
        usuario.getEstablecimientosFavoritos().clear();
        sis.marcarFavoritoEstablecimiento(establecimiento, usuario);
        Assert.assertFalse("Usuario no ha marcado el establecimiento como favorito sin sesión iniciada", usuario.getEstablecimientosFavoritos().contains(establecimiento));
    }

    @Test
    public void testDarLikeEstablecimiento() throws UsuarioNoExisteException, EstablecimientoNoExistenteException {
        // Prueba dar like a un establecimiento con sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Usuario ha dado like al establecimiento", sis.darLikeEstablecimiento(establecimiento, usuario));
        assertEquals("Número de likes actualizado", 1, establecimiento.getNumLikes());

        // Prueba dar like a un establecimiento sin sesión iniciada
        usuario.setSesionIniciada(false);
        Assert.assertFalse("Usuario no ha dado like al establecimiento sin sesión iniciada", sis.darLikeEstablecimiento(establecimiento, usuario));
        assertEquals("Número de likes no cambió", 1, establecimiento.getNumLikes());
    }

    @Test
    public void testSeguirUsuario() throws UsuarioNoExisteException {
        // Prueba seguir a un usuario con sesión iniciada y usuario existente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Usuario usuarioASeguir = sis.getUsuariosRegistrados().get(1);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        sis.seguirUsuario(usuario, usuarioASeguir);
        assertTrue("Usuario ha seguido al otro usuario", usuario.getSeguidos().contains(usuarioASeguir));

        // Prueba seguir a un usuario sin sesión iniciada
        usuario.setSesionIniciada(false);
        usuario.getSeguidos().clear();
        sis.seguirUsuario(usuario, usuarioASeguir);
        Assert.assertFalse("Usuario no ha seguido al otro usuario sin sesión iniciada", usuario.getSeguidos().contains(usuarioASeguir));

        // Prueba seguir a un usuario inexistente
        usuario.setSesionIniciada(true);
        Usuario usuarioInexistente = new Usuario("usuarioinexistente", "Usuario", "Inexistente", LocalDate.now(), 987654321, null, "usuarioinexistente@example.com", "contrasena");
        sis.seguirUsuario(usuario, usuarioInexistente);
        Assert.assertFalse("Usuario no ha seguido a un usuario inexistente", usuario.getSeguidos().contains(usuarioInexistente));
    }

    @Test
    public void testEliminarUsuario() throws UsuarioNoExisteException {
        byte[] image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        // Prueba eliminar un usuario (modo 1) con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar usuario con sesión iniciada y usuario admin", sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        Assert.assertFalse("Usuario eliminado correctamente", sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse("Eliminar usuario sin sesión iniciada", sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse("Eliminar usuario con sesión iniciada pero no admin", sis.gestionUsuario(usuarioInicial, 1, usuarioAdmin, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));
    }

    @Test
    public void testModificarUsuario() throws UsuarioNoExisteException {
        // Prueba modificar datos de usuario (modo 2) con sesión iniciada
        LocalDate nuevaFecha = LocalDate.of(2000, 1, 1);
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Modificar datos de usuario con sesión iniciada", sis.gestionUsuario(usuarioInicial, 2, null, "NuevoNombre", "NuevosApellidos", "nuevapassword", nuevaFecha));
        assertEquals("Nombre actualizado", "NuevoNombre", usuarioInicial.getNombre());
        assertEquals("Apellidos actualizados", "NuevosApellidos", usuarioInicial.getApellidos());
        assertEquals("Fecha de nacimiento actualizada", nuevaFecha, usuarioInicial.getFechaNacimiento());

        // Prueba modificar datos de usuario sin sesión iniciada
        usuarioInicial.setSesionIniciada(false);
        Assert.assertFalse("Modificar datos de usuario sin sesión iniciada", sis.gestionUsuario(usuarioInicial, 2, null, "OtroNombre", "OtrosApellidos", "otrapassword", nuevaFecha));
        assertNotEquals("Nombre no actualizado", "OtroNombre", usuarioInicial.getNombre());
        assertNotEquals("Apellidos no actualizados", "OtrosApellidos", usuarioInicial.getApellidos());
        assertNotEquals("Contraseña no actualizada", "otrapassword", usuarioInicial.getPassword());
    }

    @Test
    public void testDarDeBajaUsuario() throws UsuarioNoExisteException {
        byte[] image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        // Prueba dar de baja usuario (modo 3) con sesión iniciada
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Dar de baja usuario con sesión iniciada", sis.gestionUsuario(usuarioInicial, 3, null, null, null, null, null));
        Assert.assertFalse("Usuario eliminado correctamente", sis.existeUsuario(usuarioInicial));

        // Prueba dar de baja usuario sin sesión iniciada
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse("Dar de baja usuario sin sesión iniciada", sis.gestionUsuario(usuarioInicial, 3, null, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));
    }

    @Test
    public void testGestionActividades() throws UsuarioNoExisteException {
        Actividad actividad = sis.getActividadesRealizadas().get(0);

        // Prueba eliminar actividad sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        Assert.assertFalse("Eliminar actividad sin sesión iniciada", sis.gestionActividades(actividad, usuarioAdmin));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        Assert.assertFalse("Eliminar actividad con sesión iniciada pero no admin", sis.gestionActividades(actividad, usuarioInicial));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar actividad con sesión iniciada y usuario admin", sis.gestionActividades(actividad, usuarioAdmin));
        Assert.assertFalse("Actividad eliminada correctamente", sis.getActividadesRealizadas().contains(actividad));

    }


}
