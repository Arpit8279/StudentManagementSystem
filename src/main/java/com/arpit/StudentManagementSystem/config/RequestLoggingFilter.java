package com.arpit.StudentManagementSystem.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Logs each incoming HTTP request with method, URI, response status, and duration.
 * Runs after the JwtAuthFilter so the authenticated principal is available.
 */
@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        // Skip logging for Swagger UI and static resources
        String uri = request.getRequestURI();
        boolean isSwagger = uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!isSwagger) {
                long duration = System.currentTimeMillis() - startTime;
                log.info("[{}] {} {} → {} ({}ms)",
                        getClientIp(request),
                        request.getMethod(),
                        uri,
                        response.getStatus(),
                        duration
                );
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
