package com.s3.api.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(
        info = @Info(
                title = "API S3",
                description = "This API provides the methods to interact with AWS S3",
                version = "1.0.0",
                contact = @Contact(
                        name = "Victoria MV01",
                        url = "github.com",
                        email = "fake@email.com"
                )
        ),
        servers = {
                @Server(
                        description = "DEV Server",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "PROD Server",
                        url = "http://servidor_production.com"
                )
        },
        security = @SecurityRequirement(
                name = "security token"
        )
)

// Security is added in case of Spring Security usage
@SecurityScheme(
        name = "security token",
        description = "security token for my API",
        type = SecuritySchemeType.HTTP,
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
