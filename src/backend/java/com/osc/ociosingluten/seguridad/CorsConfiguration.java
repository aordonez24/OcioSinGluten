package com.osc.ociosingluten.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/ociosingluten/**") // ajusta el mapeo según tu ruta de API
                        .allowedOrigins("http://localhost:3000") // ajusta el origen permitido según tu aplicación
                        .allowedMethods("GET", "POST", "PUT", "DELETE") // ajusta los métodos permitidos según tu API
                        .allowCredentials(true); // si necesitas permitir credenciales en las solicitudes
            }
        };
    }
}
