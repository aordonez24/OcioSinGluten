package com.osc.ociosingluten.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {
        "com.osc.ociosingluten.repositorio",
        "com.osc.ociosingluten.servicio"
})

@EntityScan(basePackages = "com/osc/ociosingluten/modelo")
@EnableCaching
public class OcioSinGlutenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcioSinGlutenApplication.class, args);
    }


}
