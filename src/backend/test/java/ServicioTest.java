import com.osc.ociosingluten.app.OcioSinGlutenApplication;
import com.osc.ociosingluten.servicio.ServicioOcioSinGluten;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes =com.osc.ociosingluten.app.OcioSinGlutenApplication.class)
public class ServicioTest {

    @Autowired
    ServicioOcioSinGluten servicio;

    @Test
    public void testAccesoServicio(){
        Assertions.assertThat(servicio).isNotNull();
    }
}
