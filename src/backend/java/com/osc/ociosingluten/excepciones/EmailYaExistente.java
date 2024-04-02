package excepciones;

public class EmailYaExistente extends Exception{
    public EmailYaExistente(String message) {
        super(message);
    }
}
