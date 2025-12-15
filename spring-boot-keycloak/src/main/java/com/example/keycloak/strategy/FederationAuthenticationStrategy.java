package com.example.keycloak.strategy;

import com.example.keycloak.dto.LoginRequest;
import com.example.keycloak.dto.LoginResponse;
import com.example.keycloak.service.RemoteFederationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Strategy cho Federation Authentication
 * Sử dụng Remote User Federation (LDAP, AD, External API) để authenticate
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FederationAuthenticationStrategy implements AuthenticationStrategy {
    
    private final RemoteFederationService remoteFederationService;
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.resource}")
    private String clientId;
    
    @Override
    public LoginResponse authenticate(LoginRequest request) throws AuthenticationException {
        try {
            log.info("Authenticating user {} with Federation Provider", request.getUsername());
            
            // Validate với Remote User Federation Provider
            boolean isValid = remoteFederationService.validateCredentials(
                request.getUsername(), 
                request.getPassword()
            );
            
            if (!isValid) {
                throw new AuthenticationException(
                    "Invalid credentials from federation", 
                    "FEDERATION_AUTH_FAILED"
                );
            }
            
            // Nếu valid, tạo token từ Keycloak
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
                    .message("Login successful with Remote User Federation")
                    .build();
                    
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Federation authentication failed for user {}: {}", request.getUsername(), e.getMessage());
            throw new AuthenticationException(
                "Federation authentication failed", 
                "FEDERATION_ERROR", 
                e
            );
        }
    }
    
    @Override
    public boolean supports(String authType) {
        return "federation".equalsIgnoreCase(authType) || 
               "ldap".equalsIgnoreCase(authType) ||
               "remote".equalsIgnoreCase(authType);
    }
}

