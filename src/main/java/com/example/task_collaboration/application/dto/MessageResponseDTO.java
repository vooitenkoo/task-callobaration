package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Message response data")
public record MessageResponseDTO(
        @Schema(description = "Message unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Message content", example = "Hello team! How is the project going?")
        String content,
        @Schema(description = "Message sent timestamp", example = "2024-01-15T10:30:00Z")
        Instant sentAt,
        @Schema(description = "Whether the message has been read", example = "false")
        boolean isRead,
        @Schema(description = "Project ID where the message was sent", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID projectId,
        @Schema(description = "Message sender information")
        SenderDTO sender
) {
    @Schema(description = "Message sender information")
    public record SenderDTO(
            @Schema(description = "Sender user ID", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID id,
            @Schema(description = "Sender username", example = "john_doe")
            String name
    ) {}
} 