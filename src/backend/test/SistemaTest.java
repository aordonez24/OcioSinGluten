import modelo.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;

import static constantes.Constantes.SALT_LENGTH;
import static org.junit.Assert.*;

@SpringBootTest
public class SistemaTest {

    Sistema sis;
    Usuario usuarioInicial;
    Usuario usuarioAdmin;

    @Before
    public void setUp() {
        sis = new Sistema();
        Image image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        Establecimiento establecimiento1 = new Establecimiento("EstPrueba1", 670988953, "aaaaaaaaaaaaaa");
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
    public void testIniciarSesion() {

        //iniciar sesión con un usuario válido y una contraseña correcta
        assertTrue("Usuario válido y contraseña correcta", sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword()));
        assertTrue("Sesión iniciada", sis.getUsuariosRegistrados().get(0).isSesionIniciada());
        sis.cerrarSesion(sis.getUsuariosRegistrados().get(0));
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
        //cerrar sesión con un usuario existente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        assertTrue("Cerrando sesión de usuario existente", sis.cerrarSesion(usuario));
        assertFalse("Sesión cerrada", usuario.isSesionIniciada());
        assertTrue("Sesión cerrada correctamente", usuario.isSesionCerrada());

        //cerrar sesión con un usuario que no existe
        Usuario usuarioNoExistente = new Usuario("noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        assertFalse("Cerrando sesión de usuario no existente", sis.cerrarSesion(usuarioNoExistente));
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
        assertTrue("Registro de nuevo usuario válido", sis.registro("nuevousuario", "Nuevo", "Usuario", LocalDate.now(), 123456789, null, "nuevo@example.com", "contrasena"));
        assertTrue("Nuevo usuario registrado", existeUsuario("nuevo@example.com"));

        //registrar un usuario con un email ya registrado
        assertFalse("Registro con email ya registrado", sis.registro("otrousuario", "Otro", "Usuario", LocalDate.now(), 987654321, null, "aor00039@red.ujaen.es", "contrasena"));

        //registrar un usuario con un username ya registrado
        assertFalse("Registro con username ya registrado", sis.registro("aor00039", "Alvaro", "Ordoñez", LocalDate.now(), 987654321, null, "otro@example.com", "contrasena"));

        //registrar un usuario con email y username ya registrados
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

    @Test
    public void testBuscarUsuario() {
        //buscar un usuario con sesión iniciada y que existe
        Usuario usuarioConSesionIniciada = sis.getUsuariosRegistrados().get(0);
        usuarioConSesionIniciada.setSesionIniciada(true);
        assertEquals("Buscando usuario con sesión iniciada y que existe", usuarioConSesionIniciada, sis.buscarUsuario(usuarioConSesionIniciada));

        //buscar un usuario con sesión no iniciada
        Usuario usuarioSinSesion = sis.getUsuariosRegistrados().get(0);
        usuarioConSesionIniciada.setSesionIniciada(false);
        assertNull("Buscando usuario con sesión no iniciada", sis.buscarUsuario(usuarioSinSesion));

        //buscar un usuario que no existe
        Usuario usuarioNoExistente = new Usuario("noexiste", "No", "Existe", LocalDate.now(), 123456789, null, "noexiste@example.com", "contrasena");
        assertNull("Buscando usuario que no existe", sis.buscarUsuario(usuarioNoExistente));
    }

    @Test
    public void testBuscarEstablecimiento() {
        //buscar un establecimiento existente
        Establecimiento establecimientoExistente = sis.getEstablecimientosRegistrados().get(0);
        assertEquals("Establecimiento existente encontrado", establecimientoExistente, sis.buscarEstablecimiento(establecimientoExistente));

        //buscar un establecimiento que no existe
        Establecimiento establecimientoNoExistente = new Establecimiento("NombreNoExistente", 345434857, "jjjjjjjjjjjjjj");
        assertNull("Establecimiento no existente no encontrado", sis.buscarEstablecimiento(establecimientoNoExistente));
    }

    @Test
    public void testAnadirComentario() {
        //añadir comentario con sesión iniciada y texto válido
        Usuario usuario1 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario1.getEmail(), usuario1.getPassword());
        String texto1 = "Este es un comentario válido";
        assertTrue("Añadiendo comentario con sesión iniciada y texto válido", sis.anadirComentario(texto1, sis.getEstablecimientosRegistrados().get(0), usuario1));

        //añadir comentario con sesión no iniciada
        Usuario usuario2 = sis.getUsuariosRegistrados().get(0);
        sis.cerrarSesion(usuario2);
        String texto2 = "Este comentario no debería añadirse";
        assertFalse("Intento de añadir comentario con sesión no iniciada", sis.anadirComentario(texto2, sis.getEstablecimientosRegistrados().get(0), usuario2));

        //añadir comentario con texto vacío
        Usuario usuario3 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario3.getEmail(), usuario3.getPassword());
        String texto3 = "";
        assertFalse("Intento de añadir comentario con texto vacío", sis.anadirComentario(texto3, sis.getEstablecimientosRegistrados().get(0), usuario3));

        //añadir comentario con un texto que excede el límite de caracteres
        Usuario usuario4 = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario4.getEmail(), usuario4.getPassword());
        // Crear texto con más de 140 caracteres
        String texto4 = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        assertFalse("Intento de añadir comentario con texto que excede el límite de caracteres", sis.anadirComentario(texto4, sis.getEstablecimientosRegistrados().get(0), usuario4));
    }

    @Test
    public void testEliminarComentario() {
        //eliminar comentario con un comentario existente y sesión iniciada
        Comentario comentario = sis.getComentariosRealizados().get(0);
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Eliminar comentario existente con sesión iniciada", sis.eliminarComentario(comentario, usuarioInicial));
        assertFalse("Comentario eliminado", sis.getComentariosRealizados().contains(comentario));

        //eliminar comentario con sesión no iniciada
        Usuario usuarioSinSesion = new Usuario("sinSesion", "Sin", "Sesión", LocalDate.now(), 123456789, null, "sin@sesion.com", "contrasena");
        assertFalse("Eliminar comentario con sesión no iniciada", sis.eliminarComentario(comentario, usuarioSinSesion));

        //eliminar comentario con comentario no existente
        Comentario comentarioNoExistente = new Comentario("No existe", usuarioInicial);
        assertFalse("Eliminar comentario no existente", sis.eliminarComentario(comentarioNoExistente, usuarioInicial));
    }

    @Test
    public void testEditarComentario() {
        //editar comentario con usuario y sesión válidos y texto diferente
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Comentario comentario = sis.getComentariosRealizados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Editando comentario con usuario y sesión válidos y texto diferente", sis.editarComentario(comentario, "Nuevo texto", usuario));
        assertEquals("Texto editado correctamente", "Nuevo texto", comentario.getMensaje());

        //editar comentario con usuario y sesión válidos y texto igual
        assertFalse("Editando comentario con usuario y sesión válidos y texto igual", sis.editarComentario(comentario, "Nuevo texto", usuario));
        assertEquals("Texto no cambió", "Nuevo texto", comentario.getMensaje());

        //editar comentario con usuario no autorizado
        Usuario usuarioNoAutorizado = new Usuario("noautorizado", "No", "Autorizado", LocalDate.now(), 123456789, null, "noautorizado@example.com", "contrasena");
        assertFalse("Editando comentario con usuario no autorizado", sis.editarComentario(comentario, "Nuevo texto 2", usuarioNoAutorizado));

        //editar comentario con usuario autorizado pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        assertFalse("Editando comentario con usuario autorizado pero sin sesión iniciada", sis.editarComentario(comentario, "Nuevo texto 3", usuario));

        //editar comentario con comentario nulo
        assertFalse("Editando comentario nulo", sis.editarComentario(null, "Nuevo texto 4", usuario));

        //editar comentario con texto nuevo nulo
        assertFalse("Editando comentario con texto nuevo nulo", sis.editarComentario(comentario, null, usuario));

        //editar comentario con usuario nulo
        assertFalse("Editando comentario con usuario nulo", sis.editarComentario(comentario, "Nuevo texto 5", null));
    }

    @Test
    public void testAnadirEstablecimiento() {
        //agregar un establecimiento válido
        sis.getEstablecimientosRegistrados().clear();
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Añadiendo establecimiento válido", sis.anadirEstablecimiento("Restaurante A", "Calle Principal 123", 123456789, usuario));
        assertEquals("Establecimiento añadido correctamente", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con nombre demasiado largo
        assertFalse("Añadiendo establecimiento con nombre demasiado largo", sis.anadirEstablecimiento("Restaurante con un nombre muy largo que excede el límite de caracteres permitido", "Calle Principal 123", 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con nombre vacío
        assertFalse("Añadiendo establecimiento con nombre vacío", sis.anadirEstablecimiento("", "Calle Principal 123", 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección demasiado larga
        assertFalse("Añadiendo establecimiento con dirección demasiado larga", sis.anadirEstablecimiento("Restaurante B", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con dirección vacía
        assertFalse("Añadiendo establecimiento con dirección vacía", sis.anadirEstablecimiento("Restaurante B", "", 123456789, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con número de teléfono incorrecto
        assertFalse("Añadiendo establecimiento con número de teléfono incorrecto", sis.anadirEstablecimiento("Restaurante C", "Calle Principal 123", 12345, usuario));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());

        //agregar un establecimiento con usuario nulo
        assertFalse("Añadiendo establecimiento con usuario nulo", sis.anadirEstablecimiento("Restaurante D", "Calle Principal 123", 123456789, null));
        assertEquals("Establecimiento no añadido", 1, sis.getEstablecimientosRegistrados().size());
    }

    @Test
    public void testEliminarEstablecimiento() {
        // Prueba eliminar un establecimiento con usuario admin y sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(1);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Eliminando establecimiento con usuario admin y sesión iniciada", sis.eliminarEstablecimiento(establecimiento, usuario));
        assertEquals("Establecimiento eliminado correctamente", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario no admin
        Usuario usuarioNoAdmin = new Usuario("noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        assertFalse("Eliminando establecimiento con usuario no admin", sis.eliminarEstablecimiento(establecimiento, usuarioNoAdmin));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario admin pero sin sesión iniciada
        usuario.setSesionIniciada(false);
        assertFalse("Eliminando establecimiento con usuario admin pero sin sesión iniciada", sis.eliminarEstablecimiento(establecimiento, usuario));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento con usuario null
        assertFalse("Eliminando establecimiento con usuario null", sis.eliminarEstablecimiento(establecimiento, null));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());

        // Prueba eliminar un establecimiento null
        assertFalse("Eliminando establecimiento null", sis.eliminarEstablecimiento(null, usuario));
        assertEquals("Establecimiento no eliminado", 0, sis.getEstablecimientosRegistrados().size());
    }

    @Test
    public void testEditarEstablecimiento() {
        //editar un establecimiento con usuario admin y sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(1);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Editando establecimiento con usuario admin y sesión iniciada", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", "Nueva dirección", 987654321));
        assertEquals("Nombre editado correctamente", "Nuevo nombre", establecimiento.getNombre());
        assertEquals("Dirección editada correctamente", "Nueva dirección", establecimiento.getDireccion());
        assertEquals("Teléfono editado correctamente", 987654321, establecimiento.getTelefono());

        //editar un establecimiento con nombre demasiado largo
        assertFalse("Editando establecimiento con nombre demasiado largo", sis.editarEstablecimiento(establecimiento, usuario, "Restaurante con un nombre muy largo que excede el límite de caracteres permitido", "Nueva dirección", 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con nombre vacío
        assertFalse("Editando establecimiento con nombre vacío", sis.editarEstablecimiento(establecimiento, usuario, "", "Nueva dirección", 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con dirección demasiado larga
        assertFalse("Editando establecimiento con dirección demasiado larga", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 987654321));
        assertEquals("Dirección no editada", "Nueva dirección", establecimiento.getDireccion());

        //editar un establecimiento con dirección vacía
        assertFalse("Editando establecimiento con dirección vacía", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", "", 987654321));
        assertEquals("Dirección no editada", "Nueva dirección", establecimiento.getDireccion());

        //editar un establecimiento con teléfono incorrecto
        assertFalse("Editando establecimiento con teléfono incorrecto", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", "Nueva dirección", 1234));
        assertEquals("Teléfono no editado", 987654321, establecimiento.getTelefono());

        //editar un establecimiento con usuario no admin
        Usuario usuarioNoAdmin = new Usuario("noadmin", "No", "Admin", LocalDate.now(), 987654321, null, "noadmin@example.com", "noadmin123");
        assertFalse("Editando establecimiento con usuario no admin", sis.editarEstablecimiento(establecimiento, usuarioNoAdmin, "Nuevo nombre", "Nueva dirección", 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con usuario admin pero sin sesión iniciada
        sis.cerrarSesion(usuario);
        assertFalse("Editando establecimiento con usuario admin pero sin sesión iniciada", sis.editarEstablecimiento(establecimiento, usuario, "Nuevo nombre", "Nueva dirección", 987654321));
        assertEquals("Nombre no editado", "Nuevo nombre", establecimiento.getNombre());

        //editar un establecimiento con establecimiento null
        assertFalse("Editando establecimiento null", sis.editarEstablecimiento(null, usuario, "Nuevo nombre", "Nueva dirección", 987654321));

        //editar un establecimiento con usuario null
        assertFalse("Editando establecimiento con usuario null", sis.editarEstablecimiento(establecimiento, null, "Nuevo nombre", "Nueva dirección", 987654321));
    }

    @Test
    public void testVisitarEstablecimiento() {
        // Prueba visitar un establecimiento con sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        sis.visitarEstablecimiento(establecimiento, usuario);
        assertTrue("Usuario ha visitado el establecimiento", usuario.getEstablecimientosVisitados().contains(establecimiento));

        // Prueba visitar un establecimiento sin sesión iniciada

        sis.cerrarSesion(usuario);
        usuario.getEstablecimientosVisitados().clear();
        sis.visitarEstablecimiento(establecimiento, usuario);
        assertFalse("Usuario no ha visitado el establecimiento sin sesión iniciada", usuario.getEstablecimientosVisitados().contains(establecimiento));
    }

    @Test
    public void testMarcarFavoritoEstablecimiento() {
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
        assertFalse("Usuario no ha marcado el establecimiento como favorito sin sesión iniciada", usuario.getEstablecimientosFavoritos().contains(establecimiento));
    }

    @Test
    public void testDarLikeEstablecimiento() {
        // Prueba dar like a un establecimiento con sesión iniciada
        Usuario usuario = sis.getUsuariosRegistrados().get(0);
        Establecimiento establecimiento = sis.getEstablecimientosRegistrados().get(0);
        sis.iniciarSesion(usuario.getEmail(), usuario.getPassword());
        assertTrue("Usuario ha dado like al establecimiento", sis.darLikeEstablecimiento(establecimiento, usuario));
        assertEquals("Número de likes actualizado", 1, establecimiento.getNumLikes());

        // Prueba dar like a un establecimiento sin sesión iniciada
        usuario.setSesionIniciada(false);
        assertFalse("Usuario no ha dado like al establecimiento sin sesión iniciada", sis.darLikeEstablecimiento(establecimiento, usuario));
        assertEquals("Número de likes no cambió", 1, establecimiento.getNumLikes());
    }

    @Test
    public void testSeguirUsuario() {
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
        assertFalse("Usuario no ha seguido al otro usuario sin sesión iniciada", usuario.getSeguidos().contains(usuarioASeguir));

        // Prueba seguir a un usuario inexistente
        usuario.setSesionIniciada(true);
        Usuario usuarioInexistente = new Usuario("usuarioinexistente", "Usuario", "Inexistente", LocalDate.now(), 987654321, null, "usuarioinexistente@example.com", "contrasena");
        sis.seguirUsuario(usuario, usuarioInexistente);
        assertFalse("Usuario no ha seguido a un usuario inexistente", usuario.getSeguidos().contains(usuarioInexistente));
    }

    @Test
    public void testEliminarUsuario() {
        Image image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        // Prueba eliminar un usuario (modo 1) con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar usuario con sesión iniciada y usuario admin", sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        assertFalse("Usuario eliminado correctamente", sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        assertFalse("Eliminar usuario sin sesión iniciada", sis.gestionUsuario(usuarioAdmin, 1, usuarioInicial, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));

        // Prueba eliminar un usuario con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        assertFalse("Eliminar usuario con sesión iniciada pero no admin", sis.gestionUsuario(usuarioInicial, 1, usuarioAdmin, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));
    }

    @Test
    public void testModificarUsuario() {
        // Prueba modificar datos de usuario (modo 2) con sesión iniciada
        LocalDate nuevaFecha = LocalDate.of(2000, 1, 1);
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Modificar datos de usuario con sesión iniciada", sis.gestionUsuario(usuarioInicial, 2, null, "NuevoNombre", "NuevosApellidos", "nuevapassword", nuevaFecha));
        assertEquals("Nombre actualizado", "NuevoNombre", usuarioInicial.getNombre());
        assertEquals("Apellidos actualizados", "NuevosApellidos", usuarioInicial.getApellidos());
        assertEquals("Fecha de nacimiento actualizada", nuevaFecha, usuarioInicial.getFechaNacimiento());

        // Prueba modificar datos de usuario sin sesión iniciada
        usuarioInicial.setSesionIniciada(false);
        assertFalse("Modificar datos de usuario sin sesión iniciada", sis.gestionUsuario(usuarioInicial, 2, null, "OtroNombre", "OtrosApellidos", "otrapassword", nuevaFecha));
        assertNotEquals("Nombre no actualizado", "OtroNombre", usuarioInicial.getNombre());
        assertNotEquals("Apellidos no actualizados", "OtrosApellidos", usuarioInicial.getApellidos());
        assertNotEquals("Contraseña no actualizada", "otrapassword", usuarioInicial.getPassword());
    }

    @Test
    public void testDarDeBajaUsuario() {
        Image image = null; // Aquí inicializa tu imagen si es necesario
        LocalDate fecha = LocalDate.now();
        // Prueba dar de baja usuario (modo 3) con sesión iniciada
        sis.iniciarSesion(usuarioInicial.getEmail(), usuarioInicial.getPassword());
        assertTrue("Dar de baja usuario con sesión iniciada", sis.gestionUsuario(usuarioInicial, 3, null, null, null, null, null));
        assertFalse("Usuario eliminado correctamente", sis.existeUsuario(usuarioInicial));

        // Prueba dar de baja usuario sin sesión iniciada
        usuarioInicial = new Usuario("aor00039", "Alvaro", "Ordoñez Romero", fecha, 670988953, image, "aor00039@red.ujaen.es", "123456789");
        sis.anadirUsuarioPrueba(usuarioInicial);
        assertFalse("Dar de baja usuario sin sesión iniciada", sis.gestionUsuario(usuarioInicial, 3, null, null, null, null, null));
        assertTrue("Usuario no eliminado", sis.existeUsuario(usuarioInicial));
    }

    @Test
    public void testGestionActividades() {
        Actividad actividad = sis.getActividadesRealizadas().get(0);

        // Prueba eliminar actividad sin sesión iniciada
        usuarioAdmin.setSesionIniciada(false);
        assertFalse("Eliminar actividad sin sesión iniciada", sis.gestionActividades(actividad, usuarioAdmin));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada pero no admin
        usuarioInicial.setSesionIniciada(true);
        assertFalse("Eliminar actividad con sesión iniciada pero no admin", sis.gestionActividades(actividad, usuarioInicial));
        assertTrue("Actividad no eliminada", sis.getActividadesRealizadas().contains(actividad));

        // Prueba eliminar actividad con sesión iniciada y usuario admin
        sis.iniciarSesion(usuarioAdmin.getEmail(), usuarioAdmin.getPassword());
        assertTrue("Eliminar actividad con sesión iniciada y usuario admin", sis.gestionActividades(actividad, usuarioAdmin));
        assertFalse("Actividad eliminada correctamente", sis.getActividadesRealizadas().contains(actividad));

    }


}
