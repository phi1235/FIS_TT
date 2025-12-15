package com.example.keycloak.controller;

import com.example.keycloak.dto.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Mock data for demonstration
    private final java.util.Map<String, com.example.keycloak.provider.CustomUser> users = new java.util.HashMap<>();

    public UserController() {
        users.put("remoteuser", new com.example.keycloak.provider.CustomUser("100", "remoteuser", "remote@example.com",
                "password", "Remote", "User", true));
    }

    /**
     * Lấy thông tin user hiện tại từ JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> getCurrentUser(Authentication authentication) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(authentication.getName());
        userInfo.setAuthorities(authentication.getAuthorities().toString());
        return ResponseEntity.ok(userInfo);
    }

    /**
     * Admin endpoint - chỉ user có role admin mới truy cập được
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> getAllUsers() {
        return ResponseEntity.ok("Admin access granted - All users list");
    }

    @GetMapping
    public ResponseEntity<com.example.keycloak.provider.CustomUser> getUserByField(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String id) {

        if (username != null) {
            return ResponseEntity.ok(users.values().stream()
                    .filter(u -> u.getUsername().equals(username)).findFirst().orElse(null));
        }
        if (email != null) {
            return ResponseEntity.ok(users.values().stream()
                    .filter(u -> u.getEmail().equals(email)).findFirst().orElse(null));
        }
        if (id != null) {
            // Extract plain ID if prefixed
            String plainId = id.startsWith("f:") ? id.substring(id.lastIndexOf(":") + 1) : id;
            return ResponseEntity.ok(users.values().stream()
                    .filter(u -> u.getId().equals(plainId)).findFirst().orElse(null));
        }
        return ResponseEntity.ok(null);
    }

  
}
