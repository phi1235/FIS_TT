package com.example.keycloak.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository để truy vấn users từ custom database
 */
public class CustomUserRepository {
    
    private final java.sql.Connection connection;
    
    public CustomUserRepository(java.sql.Connection connection) {
        this.connection = connection;
    }
    
    public CustomUser findByUsername(String username) {
        String sql = "SELECT id, username, email, password, first_name, last_name, enabled FROM users WHERE username = ?";
        try (java.sql.PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error fetching user by username: " + username, e);
        }
        return null;
    }
    
    public CustomUser findByEmail(String email) {
        String sql = "SELECT id, username, email, password, first_name, last_name, enabled FROM users WHERE email = ?";
        try (java.sql.PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error fetching user by email: " + email, e);
        }
        return null;
    }
    
    public CustomUser findById(String id) {
        String sql = "SELECT id, username, email, password, first_name, last_name, enabled FROM users WHERE id = ?";
        try (java.sql.PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error fetching user by id: " + id, e);
        }
        return null;
    }
    
    public List<CustomUser> searchUsers(String search) {
        String sql = "SELECT id, username, email, password, first_name, last_name, enabled FROM users WHERE " +
                     "LOWER(username) LIKE ? OR LOWER(email) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ?";
        List<CustomUser> result = new ArrayList<>();
        try (java.sql.PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String query = "%" + (search == null ? "" : search.toLowerCase()) + "%";
            pstmt.setString(1, query);
            pstmt.setString(2, query);
            pstmt.setString(3, query);
            pstmt.setString(4, query);
            
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapUser(rs));
                }
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error searching users with query: " + search, e);
        }
        return result;
    }
    
    public int count() {
        String sql = "SELECT COUNT(*) FROM users";
        try (java.sql.Statement stmt = connection.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error counting users", e);
        }
        return 0;
    }
    
    private CustomUser mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CustomUser(
            rs.getString("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getBoolean("enabled")
        );
    }
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (java.sql.SQLException e) {
            // Ignore or log
        }
    }
}

