package com.example.keycloak.config;

import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Security Configuration for Bank-Level Compliance
 * 
 * Security Features:
 * - CSRF protection enabled (Cookie-based for SPA)
 * - Strict Content Security Policy
 * - HTTPS enforcement headers
 * - Session management
 * - Role-based access control
 */
@KeycloakConfiguration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    /**
     * BCrypt Password Encoder với work factor 12 (bank-level)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);

        http
                // Authorization rules
                .authorizeRequests()
                .antMatchers("/api/auth/public/**").permitAll()
                .antMatchers("/api/auth/login/**").permitAll()
                .antMatchers("/api/auth/mfa/**").permitAll() // MFA endpoints
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/api/admin/**").hasRole("admin")
                .anyRequest().authenticated()
                .and()

                // CSRF Protection - enabled for bank security
                // Using Cookie-based token for SPA compatibility
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringAntMatchers(
                        "/api/auth/login/**", // Login endpoints don't need CSRF
                        "/api/auth/public/**")
                .and()

                // Session Management
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1) // Only 1 session per user (bank requirement)
                .maxSessionsPreventsLogin(false) // Invalidate old session
                .sessionRegistry(new SessionRegistryImpl())
                .and()
                .and()

                // Security Headers
                .headers()
                // Prevent clickjacking
                .frameOptions().deny()
                // XSS Protection
                .xssProtection().block(true)
                .and()
                // Content Type sniffing protection
                .contentTypeOptions()
                .and()
                // HSTS - Force HTTPS
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000) // 1 year
                .and()
                // Referrer Policy
                .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                .and()
                // Content Security Policy
                .contentSecurityPolicy(
                        "default-src 'self'; " +
                                "script-src 'self'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data:; " +
                                "font-src 'self'; " +
                                "connect-src 'self'; " +
                                "frame-ancestors 'none';");
    }
}
