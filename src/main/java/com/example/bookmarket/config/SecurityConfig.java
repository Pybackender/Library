package com.example.bookmarket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(@Lazy JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
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
                                "/api/v1/books/add",
                                "/api/v1/books/update",
                                "/api/v1/books/delete/**"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/books/{bookId}",
                                "/api/v1/books/search",
                                "/api/v1/books/all"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                "/api/v1/comments/add"
                        ).hasRole("USER")

                        .requestMatchers(
                                "/api/v1/comments/{commentId}",
                                "/api/v1/comments/book/**"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                "/api/v1/loans/add",
                                "/api/v1/loans/return/**"
                        ).hasRole("USER")

                        .requestMatchers(
                                "/api/v1/loans/delete/**",
                                "/api/v1/loans/update",
                                "/api/v1/loans/stats"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/loans/{loanId}",
                                "/api/v1/loans/search",
                                "/api/v1/loans/all"
                        ).hasAnyRole("USER", "ADMIN")

                        .requestMatchers(
                                "/api/v1/user/logout/**",
                                "/api/v1/user/update",
                                "/api/v1/user/refresh-token"
                        ).hasRole("USER")

                        .requestMatchers(
                                "/api/v1/user/delete/**"
                        ).hasRole("ADMIN")


                        .requestMatchers(
                                "/api/v1/librarians/logout/**",
                                "/api/v1/librarians/update",
                                "/api/v1/librarians/delete/**",
                                "/api/v1/librarians/refresh-token"
                        ).hasRole("ADMIN")

                        .requestMatchers(
                                "/api/v1/statistics"
                        ).hasRole("ADMIN")

//                        .anyRequest().authenticated()
                        .anyRequest().denyAll()
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("=== SECURITY CONFIGURATION: JWT Security enabled ===");
        logger.info("USER Role: Can view books, add comments, manage loans, manage profile");
        logger.info("ADMIN Role: Full access to all management functions");

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