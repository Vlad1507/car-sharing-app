package com.app.carsharing.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String JWT_FORMAT = "JWT";
    private static final String BEARER_SCHEME = "bearer";
    private static final String BEARER_AUTH = "bearerAuth";
    private static final String AUTHORIZATION = "Authorization";

    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme().name(AUTHORIZATION)
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat(JWT_FORMAT)
                .scheme(BEARER_SCHEME);
    }

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, getSecurityScheme()));
    }
}
