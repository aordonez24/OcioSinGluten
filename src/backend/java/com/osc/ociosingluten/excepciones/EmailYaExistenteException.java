package excepciones;

public class EmailYaExistenteException extends Exception{
    public EmailYaExistenteException(String message) {
        super(message);
    }
}
