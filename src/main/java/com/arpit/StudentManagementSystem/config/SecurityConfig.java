package com.arpit.StudentManagementSystem.config;

import com.arpit.StudentManagementSystem.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // Stateless — no HTTP session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ──────────────────────────────────
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // ── Frontend static files ─────────────────────────────
                        .requestMatchers("/", "/index.html", "/register.html",
                                "/dashboard.html", "/css/**", "/js/**",
                                "/favicon.ico").permitAll()

                        // ── Swagger / OpenAPI (no auth required) ──────────────
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml"
                        ).permitAll()

                        // ── Department endpoints ──────────────────────────────
                        // Only ADMINs can create departments
                        .requestMatchers(HttpMethod.POST, "/api/departments").hasRole("ADMIN")
                        // GET departments is public — needed by the Register page (no token yet)
                        .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()

                        // ── Student endpoints ─────────────────────────────────
                        // Only ADMINs can create or delete students directly
                        .requestMatchers(HttpMethod.POST, "/api/v1/students").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**").hasRole("ADMIN")
                        // ADMINs or STUDENTs can update
                        .requestMatchers(HttpMethod.PUT, "/api/v1/students/**")
                                .hasAnyRole("ADMIN", "STUDENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/students/**")
                                .hasAnyRole("ADMIN", "STUDENT")
                        // Any authenticated user can read students
                        .requestMatchers(HttpMethod.GET, "/api/v1/students/**").authenticated()

                        // ── Everything else requires authentication ───────────
                        .anyRequest().authenticated()
                )

                // JWT filter runs before Spring's username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Use our custom UserDetailsService + PasswordEncoder
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
