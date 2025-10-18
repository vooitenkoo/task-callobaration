package com.example.task_collaboration.infrastructure.oauth2.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "OAuth2 login response with tokens and user information")
public record OAuth2LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        @Schema(description = "User unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,
        @Schema(description = "User name", example = "John Doe")
        String name,
        @Schema(description = "User email", example = "john.doe@example.com")
        String email,
        @Schema(description = "User avatar URL", example = "https://example.com/avatar.jpg")
        String imageUrl,
        @Schema(description = "OAuth2 provider", example = "GOOGLE", allowableValues = {"GOOGLE", "GITHUB"})
        String provider,
        @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN"})
        String role
) {}
