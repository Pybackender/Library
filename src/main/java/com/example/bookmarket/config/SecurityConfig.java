package com.example.bookmarket.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(@Lazy JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        // ==================== PUBLIC ENDPOINTS ==================== //
                        .requestMatchers(
                                "/api/v1/user/login",
                                "/api/v1/user/register",
                                "/api/v1/librarians/login",
                                "/api/v1/librarians/register",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/favicon.ico"
                        ).permitAll()

                        .requestMatchers(
                                "/api/v1/books/{bookId}",
                                "/api/v1/books/search",
                                "/api/v1/books/all",
                                "/api/v1/comments/{commentId}",
                                "/api/v1/comments/book/**",
                                "/api/v1/loans/{loanId}",
                                "/api/v1/loans/search",
                                "/api/v1/loans/all"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                "/api/v1/librarians/logout/**",
                                "/api/v1/librarians/update",
                                "/api/v1/librarians/delete/**",
                                "/api/v1/librarians/refresh-token",
                                "/api/v1/statistics",
                                "/api/v1/user/delete/**",
                                "/api/v1/loans/delete/**",
                                "/api/v1/loans/update",
                                "/api/v1/loans/stats",
                                "/api/v1/books/add",
                                "/api/v1/books/update",
                                "/api/v1/books/delete/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/user/logout/**",
                                "/api/v1/user/update",
                                "/api/v1/user/refresh-token",
                                "/api/v1/comments/add",
                                "/api/v1/loans/add",
                                "/api/v1/loans/return/**"
                        ).hasRole("USER")

                        .anyRequest().denyAll()
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("=== SECURITY CONFIGURATION: JWT Security enabled ===");
        log.info("USER Role: Can view books, add comments, manage loans, manage profile");
        log.info("ADMIN Role: Full access to all management functions");
        log.info("CORS Configuration: Enabled via separate CorsConfig class");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}