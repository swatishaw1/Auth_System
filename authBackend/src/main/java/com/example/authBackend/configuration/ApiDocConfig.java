package com.example.authBackend.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Authentication App",
                version = "1.0",
                description = "API documentation for the Authentication Backend",
                contact = @Contact(
                        name = "Swati Shaw",
                        url = "https://github.com/swatishaw1",
                        email = "swatishaw104@gmail.com"
                ),
                summary = "This is a multitenant Auth System Where we used JWT + Refresh Token + OAUTH2"
        ),
        security = {
                @SecurityRequirement(
                        name = "bearerAuth"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer", // Authorization: bearer <token>
        bearerFormat = "JWT"
)
public class ApiDocConfig {
}
