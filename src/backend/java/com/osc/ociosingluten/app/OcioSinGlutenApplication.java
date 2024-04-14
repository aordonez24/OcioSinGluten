package com.osc.ociosingluten.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.osc.ociosingluten.servicio",
        "com.osc.ociosingluten.repositorio",
        "com.osc.ociosingluten.chat",
        "com.osc.ociosingluten.controlador"
})
@EntityScan(basePackages = "com/osc/ociosingluten/modelo")
@EnableJpaRepositories(basePackages = "com.osc.ociosingluten.repositorio")
@ComponentScan(basePackages = "com.osc.ociosingluten.repositorio")
@ComponentScan(basePackages = "com.osc.ociosingluten.servicio")
@ComponentScan(basePackages = "com.osc.ociosingluten.chat")
@ComponentScan(basePackages = "com.osc.ociosingluten.controlador")
@EnableCaching
public class OcioSinGlutenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcioSinGlutenApplication.class, args);
    }


}
