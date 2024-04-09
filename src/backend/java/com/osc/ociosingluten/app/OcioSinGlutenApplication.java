package com.osc.ociosingluten.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = {
        "com.osc.ociosingluten.repositorio",
        "com.osc.ociosingluten.servicio"
})

@EntityScan(basePackages = "com/osc/ociosingluten/modelo")
@ComponentScan("com.osc.ociosingluten.repositorio")
@ComponentScan("com.osc.ociosingluten.servicio")
@EnableCaching
public class OcioSinGlutenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcioSinGlutenApplication.class, args);
    }


}
