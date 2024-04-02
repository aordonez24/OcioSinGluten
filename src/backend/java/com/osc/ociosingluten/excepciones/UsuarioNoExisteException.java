package excepciones;

public class UsuarioNoExisteException extends Exception{
    public UsuarioNoExisteException(String message) {
        super(message);
    }
}
