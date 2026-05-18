package com.hotel.config;

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

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Hotel Management System API")
                .description("Production-grade RESTful API for hotel operations — rooms, reservations, billing, housekeeping, and analytics.")
                .version("v1.0.0")
                .contact(new Contact()
                    .name("Hotel Engineering Team")
                    .email("engineering@hotel.com"))
                .license(new License().name("Proprietary")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development"),
                new Server().url("https://api.hotel.com").description("Production")
            ))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Provide the JWT access token obtained from /api/v1/auth/login")));
    }
}
