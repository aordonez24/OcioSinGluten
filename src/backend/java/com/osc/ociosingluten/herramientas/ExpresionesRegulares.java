package herramientas;

public class ExpresionesRegulares {
    private ExpresionesRegulares() {
    }

    public static final String TLF = "^(\\+34|0034|34)?[6789]\\d{8}$";
    public static final String EMAIL = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String CONTRASENA = "^(?=.*[A-Z])(?=.*\\d).{8,}$";
    public static final String CODPOSTAL = "^\\d{5}$";

}
