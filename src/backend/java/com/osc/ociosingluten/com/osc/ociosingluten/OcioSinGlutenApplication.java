package com.osc.ociosingluten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com/osc/ociosingluten/modelo")
public class OcioSinGlutenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcioSinGlutenApplication.class, args);
    }

}
