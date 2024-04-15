package com.osc.ociosingluten;

import com.osc.ociosingluten.controlador.DTO.UsuarioDTO;
import com.osc.ociosingluten.modelo.Usuario;
import jakarta.annotation.PostConstruct;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;

import java.nio.MappedByteBuffer;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@SpringBootTest(classes=com.osc.ociosingluten.app.OcioSinGlutenApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
public class TestRest {

    @LocalServerPort
    int localPort;

    @Autowired
    MappingJackson2HttpMessageConverter springBootJacksonConverter;
    TestRestTemplate restTemplate;

    @PostConstruct
    void crearRestTemplateBuilder(){
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                .rootUri("http://localhost: " + localPort + "/ociosingluten")
                .additionalMessageConverters(List.of(springBootJacksonConverter));
        restTemplate = new TestRestTemplate(restTemplateBuilder);
    }

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

    @Test
    public void anadirYVerUsuario(){
        byte[] fotoperfil = null;

        UsuarioDTO usuario = new UsuarioDTO(generarDNIAleatorio(), "Ord24_", "Alvaro", "Ordoñez Romero", LocalDate.of(2002, 10, 24), 670988953, fotoperfil, generarCorreoAleatorio(), "alonsismoF13");


    }

}
