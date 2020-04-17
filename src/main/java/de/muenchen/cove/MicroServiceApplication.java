/*
 * Copyright (c): it@M - Dienstleister für Informations- und Telekommunikationstechnik
 * der Landeshauptstadt München, 2020
 */
package de.muenchen.cove;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


/**
 * Application class for starting the micro-service.
 */
@Configuration
@ComponentScan(basePackages = {
        "org.springframework.data.jpa.convert.threeten",
        "de.muenchen.cove"
        })
@EntityScan(basePackages = {
        "org.springframework.data.jpa.convert.threeten",
        "de.muenchen.cove"
        })
@EnableJpaRepositories(basePackages = {
        "de.muenchen.cove"
        })
@EnableAutoConfiguration
public class MicroServiceApplication {


    public static void main(String[] args) {
        SpringApplication.run(MicroServiceApplication.class, args);
    }


}
