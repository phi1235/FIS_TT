package com.example.keycloak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KeycloakIntegrationApplication {
    public static void main(String[] args) {
        SpringApplication.run(KeycloakIntegrationApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver();
    }
}
