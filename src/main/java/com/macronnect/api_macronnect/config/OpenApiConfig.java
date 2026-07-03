package com.macronnect.api_macronnect.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI apiMacronnectOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Macronnect - Sistema de Ventas")
                        .description("API REST para administrar clientes, artículos y ventas, "
                                + "con autenticación JWT. Prueba técnica Backend Java/Spring Boot.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Eric Amezcua")
                                .email("tu-correo@ejemplo.com")))
                // Aplica el requerimiento de seguridad a nivel global
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                // Define el esquema de seguridad "bearerAuth"
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Pega aquí el token que devuelve POST /auth/login "
                                                + "(sin la palabra 'Bearer', Swagger la agrega sola)")));
    }
}