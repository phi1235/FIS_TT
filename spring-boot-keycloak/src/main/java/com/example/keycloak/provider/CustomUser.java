package com.example.keycloak.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity đại diện cho User trong custom database
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomUser {
    private String id;
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private boolean enabled;
}

