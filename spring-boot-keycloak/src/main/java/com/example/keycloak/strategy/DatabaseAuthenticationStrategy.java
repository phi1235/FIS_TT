package com.example.keycloak.strategy;

import com.example.keycloak.dto.LoginRequest;
import com.example.keycloak.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Strategy cho Database Authentication
 * Sử dụng Keycloak internal database để authenticate
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseAuthenticationStrategy implements AuthenticationStrategy {
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Override
    public LoginResponse authenticate(LoginRequest request) throws AuthenticationException {
        try {
            log.info("Authenticating user {} with Database Provider", request.getUsername());
            
            Keycloak keycloak = KeycloakBuilder.builder()
                    .serverUrl(keycloakServerUrl)
                    .realm(realm)
                    .clientId(clientId)
                    .username(request.getUsername())
                    .password(request.getPassword())
                    .build();
            
            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
            
            return LoginResponse.builder()
                    .accessToken(tokenResponse.getToken())
                    .refreshToken(tokenResponse.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(tokenResponse.getExpiresIn())
                    .username(request.getUsername())
                    .message("Login successful with Database Provider")
                    .build();
                    
        } catch (Exception e) {
            log.error("Database authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new AuthenticationException(
                "Invalid credentials", 
                "INVALID_CREDENTIALS", 
                e
            );
        }
    }
    
    @Override
    public boolean supports(String authType) {
        return "database".equalsIgnoreCase(authType);
    }
}

