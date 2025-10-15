package com.example.task_collaboration.application.mapper;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response with tokens")
public record AuthResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken, 
        @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
) {

}