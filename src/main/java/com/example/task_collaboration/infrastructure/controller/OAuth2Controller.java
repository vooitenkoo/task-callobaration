package com.example.task_collaboration.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth2")
@Tag(name = "OAuth2", description = "OAuth2 authentication endpoints")
public class OAuth2Controller {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    @GetMapping("/providers")
    @Operation(summary = "Get OAuth2 provider information", 
               description = "Returns available OAuth2 providers and their login URLs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved provider information")
    })
    public ResponseEntity<Map<String, Object>> getProviders() {
        Map<String, Object> providers = Map.of(
                "google", Map.of(
                        "name", "Google",
                        "loginUrl", "/oauth2/authorization/google",
                        "enabled", !googleClientId.equals("your-google-client-id")
                ),
                "github", Map.of(
                        "name", "GitHub", 
                        "loginUrl", "/oauth2/authorization/github",
                        "enabled", !githubClientId.equals("your-github-client-id")
                )
        );

        return ResponseEntity.ok(providers);
    }

    @GetMapping("/login/google")
    @Operation(summary = "Get Google OAuth2 login URL", 
               description = "Returns the Google OAuth2 authorization URL")
    public ResponseEntity<Map<String, String>> getGoogleLoginUrl() {
        return ResponseEntity.ok(Map.of(
                "loginUrl", "/oauth2/authorization/google",
                "provider", "Google"
        ));
    }

    @GetMapping("/login/github")
    @Operation(summary = "Get GitHub OAuth2 login URL", 
               description = "Returns the GitHub OAuth2 authorization URL")
    public ResponseEntity<Map<String, String>> getGitHubLoginUrl() {
        return ResponseEntity.ok(Map.of(
                "loginUrl", "/oauth2/authorization/github",
                "provider", "GitHub"
        ));
    }
}
