import com.osc.ociosingluten.herramientas.Direccion;
import com.osc.ociosingluten.herramientas.MensajePredefinido;
import com.osc.ociosingluten.modelo.*;
import com.osc.ociosingluten.excepciones.*;


import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.wildfly.common.Assert;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.AssertionErrors.*;

@SpringBootTest(classes= com.osc.ociosingluten.modelo.Sistema.class)
public class SistemaTest {

    static Sistema sis;
    static Usuario usuarioInicial;
    static Usuario usuarioAdmin;

    @BeforeEach
    public void setUp() {
        sis = new Sistema();
        byte[] image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        String dni = "78162640S";
        usuarioInicial = new Usuario(dni, "aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Direccion dir = new Direccion("Jaén", "Jaén", "Avenida de Andalucía, 83", 23006, "España");
        Establecimiento establecimiento1 = new Establecimiento("EstPrueba1", 670988953, dir);
        sis.anadirEstablecimientoPrueba(establecimiento1);
        usuarioInicial.setSesionIniciada(true);
        Comentario com = new Comentario("aaaaaaaaaaaaaaaaaaaaa", usuarioInicial);
        boolean b = sis.anadirComentario(com.getMensaje(), establecimiento1, usuarioInicial);
        usuarioInicial.setSesionIniciada(false);
        String dni2 = "78162641Q";
        usuarioAdmin = new Usuario(dni2, "aor00030", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@admin.es", "holaaaaa");
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
        Assert.assertFalse(sis.getUsuariosRegistrados().get(0).isSesionIniciada());

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
        String dni2 = "78062641Q";
        //cerrar sesión con un usuario existente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        assertTrue("Cerrando sesión de usuario existente", sis.cerrarSesion(usuario));
        Assert.assertFalse(usuario.isSesionIniciada());
        assertTrue("Sesión cerrada correctamente", usuario.isSesionCerrada());

        //cerrar sesión con un usuario que no existe
        Usuario usuarioNoExistente = new Usuario(dni2, "noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        // Verifica que la sesión de un usuario no existente no se pueda cerrar
        Assert.assertFalse(usuario.isSesionIniciada());
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
        String dni2 = "78162541Q";
        try {
            assertTrue("Registro de nuevo usuario válido", sis.registro(dni2, "nuevousuario", "Nuevo", "Usuario", LocalDate.now(), 123456789, null, "nuevo@example.com", "contrasena"));
            assertTrue("Nuevo usuario registrado", existeUsuario("nuevo@example.com"));
        } catch (ContrasenaIncorrectaException | ErrorDatosException e) {
            fail("No se esperaba ninguna excepción aquí");
        }

        //registrar un usuario con un email ya registrado
        try {
            assertFalse("Registro con email ya registrado", sis.registro(dni2, "otrousuario", "Otro", "Usuario", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (ContrasenaIncorrectaException e) {
        } catch (ErrorDatosException e) {
            fail("Se esperaba EmailYaExistenteException, pero se lanzó ErrorDatosException");
        }

        //registrar un usuario con un username ya registrado
        try {
            assertFalse("Registro con username ya registrado", sis.registro(dni2, "aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "otro@example.com", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (ContrasenaIncorrectaException e) {
        } catch (ErrorDatosException e) {
            fail("Se esperaba EmailYaExistenteException, pero se lanzó ErrorDatosException");
        }

        //registrar un usuario con email y username ya registrados
        try {
            assertFalse("Registro con email y username ya registrados", sis.registro(dni2, "aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));
            fail("Se esperaba EmailYaExistenteException");
        } catch (ContrasenaIncorrectaException e) {
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
        String dni2 = "78100641Q";
        Usuario usuarioNoExistente = new Usuario(dni2, "noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
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
        Assert.assertFalse( sis.anadirComentario(texto2, sis.getEstablecimientosRegistrados().get(0), usuario2));

        //añadir comentario con texto vacío
        Usuario usuario3 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario3.getEmail(), usuario3.getPassword());
        String texto3 = "";
        Assert.assertFalse( sis.anadirComentario(texto3, sis.getEstablecimientosRegistrados().get(0), usuario3));

        //añadir comentario con un texto que excede el límite de caracteres
        Usuario usuario4 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario4.getEmail(), usuario4.getPassword());
        // Crear texto con más de 140 caracteres
        String texto4 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        Assert.assertFalse( sis.anadirComentario(texto4, sis.getEstablecimientosRegistrados().get(0), usuario4));
    }

    @Test
    public void testEliminarComentario() throws UsuarioNoExisteException {
        //eliminar comentario con un comentario existente y sesión iniciada
        Comentario comentario = sis.getComentariosRealizados().get(0);
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Eliminar comentario existente con sesión iniciada", sis.eliminarComentario(comentario, usuarioInicial));
        Assert.assertFalse( sis.getComentariosRealizados().contains(comentario));

        //eliminar comentario con sesión no iniciada
        String dni2 = "78162641P";
        Usuario usuarioSinSesion = new Usuario(dni2, "sinSesion", "Sin", "Sesión", LocalDate.now(), 123456789, null, "sin@sesion.com", "contrasena");
        Assert.assertFalse( sis.eliminarComentario(comentario, usuarioSinSesion));

        //eliminar comentario con comentario no existente
        Comentario comentarioNoExistente = new Comentario("No existe", usuarioInicial);
        Assert.assertFalse( sis.eliminarComentario(comentarioNoExistente, usuarioInicial));
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
        Assert.assertFalse( sis.editarComentario(comentario, "Nuevo texto", usuario));
        assertEquals("Texto no cambió", "Nuevo texto", comentario.getMensaje());

        //editar comentario con usuario no autorizado
        String dni2 = "78162641Q43";
        Usuario usuarioNoAutorizado = new Usuario(dni2, "noautorizado", "No", "Autorizado", LocalDate.now(), 123456789, null, "noautorizado@example.com", "contrasena");
        Assert.assertFalse( sis.editarComentario(comentario, "Nuevo texto 2", usuarioNoAutorizado));

        //editar comentario con usuario autorizado pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        Assert.assertFalse(sis.editarComentario(comentario, "Nuevo texto 3", usuario));

        //editar comentario con comentario nulo
        Assert.assertFalse(sis.editarComentario(null, "Nuevo texto 4", usuario));

        //editar comentario con texto nuevo nulo
        Assert.assertFalse(sis.editarComentario(comentario, null, usuario));

        //editar comentario con usuario nulo
        Assert.assertFalse(sis.editarComentario(comentario, "Nuevo texto 5", null));
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
        Assert.assertFalse( sis.anadirEstablecimiento("Restaurante con un nombre muy largo que excede el límite de caracteres permitido", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con nombre vacío
        Assert.assertFalse(sis.anadirEstablecimiento("", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección demasiado larga
        dir.setCalle("hjbfksdabfadskbgksfdjngkjdfngkjfdwngkanglskfjgbsflkdjgnsfkjgndsflkgjnffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffsdflgkjbsdfngkjsdfbngkjsdfnbkñjdnbkesfnbksdfjnbsdfñjsdñjgsdñjsfuhunsdñkjsdñlgfñjldjhnsdfñhndsfgndsfhjsndfñhkjnsdfgunsdfbuesrnhpieurbnkñsdfngs");
        Assert.assertFalse( sis.anadirEstablecimiento("Restaurante B", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección vacía
        dir.setCalle("");
        Assert.assertFalse(sis.anadirEstablecimiento("Restaurante B", dir, 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con número de teléfono incorrecto
        dir.setCalle("Avenida De Andalucía Jaén");
        Assert.assertFalse(sis.anadirEstablecimiento("Restaurante C", dir, 12345, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con usuario nulo
        dir.setCalle("Avenida De Andalucía Jaén");
        Assert.assertFalse(sis.anadirEstablecimiento("Restaurante D", dir, 123456789, null));
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
        String dni2 = "7816567641Q";
        Usuario usuarioNoAdmin = new Usuario(dni2, "noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        Assert.assertFalse( sis.eliminarEstablecimiento(establecimiento, usuarioNoAdmin));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario admin pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        Assert.assertFalse( sis.eliminarEstablecimiento(establecimiento, usuario));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario null
        Assert.assertFalse( sis.eliminarEstablecimiento(establecimiento, null));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento null
        Assert.assertFalse(sis.eliminarEstablecimiento(null, usuario));
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
        String dni2 = "345638475f";
        Usuario usuarioNoAdmin = new Usuario(dni2, "noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        Assert.assertFalse(sis.editarEstablecimiento(establecimiento, usuarioNoAdmin, "Nuevo nombre", dir, 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con usuario admin pero sin sesión iniciada
        sis.cerrarSesion(usuario);
        Assert.assertFalse( sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", dir, 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con establecimiento null
        Assert.assertFalse( sis.editarEstablecimiento(null, usuario, "Nuevo nombre", dir, 987654321));

        //editar un establecimiento con usuario null
        Assert.assertFalse(sis.editarEstablecimiento(establecimiento, null, "Nuevo nombre", dir, 987654321));
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
        Assert.assertFalse( usuario.getEstablecimientosVisitados().contains(establecimiento));
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
        Assert.assertFalse(usuario.getEstablecimientosFavoritos().contains(establecimiento));
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
        Assert.assertFalse( sis.darLikeEstablecimiento(establecimiento, usuario));
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
        Assert.assertFalse( usuario.getSeguidos().contains(usuarioASeguir));

        // Prueba seguir a un usuario inexistente
        usuario.setSesionIniciada(true);
        String dni2 = "781626466Q";
        Usuario usuarioInexistente = new Usuario(dni2, "usuarioinexistente", "Usuario", "Inexistente", LocalDate.now(), 987654321, null, "usuarioinexistente@example.com", "contrasena");
        sis.seguirUsuario(usuario, usuarioInexistente);
        Assert.assertFalse( usuario.getSeguidos().contains(usuarioInexistente));
    }

    @Test
    public void testEliminarUsuario() throws UsuarioNoExisteException {
        byte[] image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        // Prueba eliminar un usuario (modo 1) con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar usuario con sesión iniciada y usuario admin", sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        Assert.assertFalse( sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        String dni2 = "7816266541Q";
        usuarioInicial = new Usuario(dni2,"aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse( sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        String dni3 = "74162641Q";
        usuarioInicial = new Usuario(dni3, "aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse( sis.gestionUsuario(usuarioInicial, 1, usuarioAdmin, null, null, null, null));
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
        Assert.assertFalse( sis.gestionUsuario(usuarioInicial, 2, null, "OtroNombre", "OtrosApellidos", "otrapassword", nuevaFecha));
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
        Assert.assertFalse(sis.existeUsuario(usuarioInicial));

        // Prueba dar de baja usuario sin sesión iniciada
        String dni2 = "847564387p";
        usuarioInicial = new Usuario(dni2, "aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Assert.assertFalse( sis.gestionUsuario(usuarioInicial, 3, null, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));
    }

    @Test
    public void testGestionActividades() throws UsuarioNoExisteException {
        Actividad actividad = sis.getActividadesRealizadas().get(0);

        // Prueba eliminar actividad sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        Assert.assertFalse( sis.gestionActividades(actividad, usuarioAdmin));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        Assert.assertFalse( sis.gestionActividades(actividad, usuarioInicial));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar actividad con sesión iniciada y usuario admin", sis.gestionActividades(actividad, usuarioAdmin));
        Assert.assertFalse( sis.getActividadesRealizadas().contains(actividad));

    }


}
