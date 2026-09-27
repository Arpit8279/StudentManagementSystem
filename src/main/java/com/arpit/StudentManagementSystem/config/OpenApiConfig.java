package com.arpit.StudentManagementSystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                // Register the Bearer security scheme so the "Authorize" button appears in Swagger UI
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME,
                                new SecurityScheme()
                                        .name(BEARER_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT token here (obtained from POST /api/v1/auth/login)")
                        )
                )
                // Apply Bearer auth globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    private Info apiInfo() {
        return new Info()
                .title("Student Management System API")
                .description("""
                        REST API for managing students, departments, and user authentication.
                        
                        **Authentication Flow:**
                        1. `POST /api/v1/auth/register` — Create a new student account
                        2. `POST /api/v1/auth/login` — Get a JWT Bearer token
                        3. Click **Authorize** above and paste the token to authenticate all requests
                        
                        **Role-Based Access:**
                        - `ADMIN` — Full access (create/update/delete)
                        - `STUDENT` — Read access + own profile update
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Arpit Sahu")
                        .email("arpit@example.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
