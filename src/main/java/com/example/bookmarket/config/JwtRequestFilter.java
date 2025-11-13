package com.example.bookmarket.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Autowired
    public JwtRequestFilter(JwtUtil jwtUtil, @Lazy UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // اگر درخواست برای endpointهای عمومی است، فیلتر را رد کن
        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        // استخراج توکن از هدر
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwtToken);
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired");
                sendErrorResponse(response, "JWT Token has expired", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            } catch (Exception e) {
                logger.warn("Invalid JWT token");
                sendErrorResponse(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        // اگر endpoint نیاز به احراز هویت دارد و توکن ارائه نشده
        if (requiresAuthentication(requestURI) && username == null) {
            sendErrorResponse(response, "JWT token is required", HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // اعتبارسنجی توکن
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
                    Claims claims = jwtUtil.extractAllClaims(jwtToken);

                    List<String> roles = claims.get("roles", List.class);
                    if (roles != null) {
                        var authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.debug("Authenticated user: {} with roles: {}", username, roles);
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot set user authentication: {}", e.getMessage());
                sendErrorResponse(response, "Authentication failed", HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String requestURI) {
        List<String> publicEndpoints = List.of(
                "/api/v1/user/login",
                "/api/v1/user/register",
                "/api/v1/librarians/login",
                "/api/v1/librarians/register",
                "/swagger-ui",
                "/swagger-ui/",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/",
                "/v3/api-docs/**",
                "/api-docs",
                "/api-docs/",
                "/api-docs/**",
                "/webjars/",
                "/webjars/**",
                "/swagger-resources",
                "/swagger-resources/",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/favicon.ico"
        );

        return publicEndpoints.stream().anyMatch(uri ->
                requestURI.equals(uri) ||
                        requestURI.startsWith(uri.replace("/**", "")) ||
                        uri.replace("/**", "").startsWith(requestURI)
        );
    }

    private boolean requiresAuthentication(String requestURI) {
        return !isPublicEndpoint(requestURI);
    }

    private void sendErrorResponse(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + message + "\"}");
    }
}