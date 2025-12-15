package com.example.keycloak.controller;

import com.example.keycloak.dto.LoginRequest;
import com.example.keycloak.dto.LoginResponse;
import com.example.keycloak.dto.MfaRequest;
import com.example.keycloak.dto.MfaSetupResponse;
import com.example.keycloak.service.AuthenticationService;
import com.example.keycloak.service.MfaService;
import com.example.keycloak.strategy.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final AuthenticationService authenticationService;
    private final MfaService mfaService;
    
    /**
     * API đăng nhập sử dụng User Provider Database
     * Sử dụng Strategy Pattern - DatabaseAuthenticationStrategy
     */
    @PostMapping("/login/database")
    public ResponseEntity<LoginResponse> loginWithDatabase(
            @Valid @RequestBody LoginRequest request) throws AuthenticationException {
        LoginResponse response = authenticationService.authenticateWithDatabase(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * API đăng nhập sử dụng Remote User Federation
     * Sử dụng Strategy Pattern - FederationAuthenticationStrategy
     */
    @PostMapping("/login/federation")
    public ResponseEntity<LoginResponse> loginWithFederation(
            @Valid @RequestBody LoginRequest request) throws AuthenticationException {
        LoginResponse response = authenticationService.authenticateWithFederation(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * API đăng nhập generic - cho phép chọn authentication type
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestParam(defaultValue = "database") String authType) throws AuthenticationException {
        LoginResponse response = authenticationService.authenticate(request, authType);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Setup MFA cho user - tạo secret và QR code
     */
    @PostMapping("/mfa/setup")
    public ResponseEntity<MfaSetupResponse> setupMfa(@RequestParam String username) {
        String secret = mfaService.generateSecret(username);
        String qrCodeUrl = mfaService.generateQrCodeUrl(username, secret, "Bank App");
        String manualEntryKey = mfaService.getSecret(username);
        
        MfaSetupResponse response = MfaSetupResponse.builder()
            .secret(secret)
            .qrCodeUrl(qrCodeUrl)
            .manualEntryKey(manualEntryKey)
            .message("Scan QR code with Google Authenticator or enter key manually")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verify MFA code
     */
    @PostMapping("/mfa/verify")
    public ResponseEntity<String> verifyMfa(@Valid @RequestBody MfaRequest request) {
        boolean isValid = mfaService.verifyCode(request.getUsername(), request.getCode());
        if (isValid) {
            return ResponseEntity.ok("{\"status\":\"success\",\"message\":\"MFA code verified\"}");
        }
        return ResponseEntity.status(401).body("{\"status\":\"error\",\"message\":\"Invalid MFA code\"}");
    }
    
    /**
     * Disable MFA cho user
     */
    @PostMapping("/mfa/disable")
    public ResponseEntity<String> disableMfa(@RequestParam String username) {
        mfaService.disableMfa(username);
        return ResponseEntity.ok("{\"status\":\"success\",\"message\":\"MFA disabled\"}");
    }
    
    /**
     * Check if MFA is enabled for user
     */
    @GetMapping("/mfa/status")
    public ResponseEntity<String> checkMfaStatus(@RequestParam String username) {
        boolean enabled = mfaService.isMfaEnabled(username);
        return ResponseEntity.ok(String.format("{\"enabled\":%s}", enabled));
    }
    
    /**
     * Public endpoint - không cần authentication
     */
    @GetMapping("/public/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Service is running");
    }
}

