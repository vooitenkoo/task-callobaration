package com.example.task_collaboration.infrastructure.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.Scopes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

        @Bean
        public OpenAPI openAPI() {
                return new OpenAPI()
                        .info(new Info()
                                .title("Task Collaboration Hub API")
                                .description("REST API documentation for Task Collaboration Hub project with OAuth2 support")
                                .version("1.0.0")
                                .contact(new Contact()
                                        .name("Your Name")
                                        .email("your.email@example.com")))
                        .servers(List.of(
                                new Server().url("http://localhost:8084").description("Local server")
                        ))
                        .components(new Components()
                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token authentication. Enter your token in the format: Bearer <token>"))
                                .addSecuritySchemes("oauth2", new SecurityScheme()
                                        .type(SecurityScheme.Type.OAUTH2)
                                        .description("OAuth2 authentication with Google and GitHub")
                                        .flows(new OAuthFlows()
                                                .authorizationCode(new OAuthFlow()
                                                        .authorizationUrl("https://accounts.google.com/o/oauth2/v2/auth")
                                                        .tokenUrl("https://www.googleapis.com/oauth2/v4/token")
                                                        .scopes(new Scopes()
                                                                .addString("openid", "OpenID Connect")
                                                                .addString("profile", "User profile information")
                                                                .addString("email", "User email address"))))))
                        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                        .addSecurityItem(new SecurityRequirement().addList("oauth2"));
        }
}