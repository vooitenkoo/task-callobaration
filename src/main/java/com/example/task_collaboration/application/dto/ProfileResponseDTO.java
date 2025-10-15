package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "User profile response data")
public record ProfileResponseDTO(
        @Schema(description = "User unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "User display name", example = "John Doe")
        String displayName,
        @Schema(description = "User avatar URL", example = "https://example.com/avatars/user123.jpg")
        String avatarUrl,
        @Schema(description = "User bio", example = "Software developer with 5 years experience")
        String bio,
        @Schema(description = "User location", example = "New York, USA")
        String location,
        @Schema(description = "User job title", example = "Senior Developer")
        String jobTitle
) {}