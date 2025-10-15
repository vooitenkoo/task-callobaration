package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "Message creation request")
public record MessageRequestDTO(
        @NotBlank(message = "Content is required")
        @Size(max = 1000, message = "Message content must not exceed 1000 characters")
        @Schema(description = "Message content", example = "Hello team! How is the project going?")
        String content,
        @Schema(description = "Project ID where the message is sent", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID projectId
) {} 