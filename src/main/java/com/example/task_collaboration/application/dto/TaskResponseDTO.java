package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Schema(description = "Task response data")
public record TaskResponseDTO(
        @Schema(description = "Task unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Task title", example = "Implement user authentication")
        String title,
        @Schema(description = "Task description", example = "Add JWT-based authentication to the API")
        String description,
        @Schema(description = "Task deadline", example = "2024-12-31T23:59:59Z")
        Instant deadline,
        @Schema(description = "Task status", example = "IN_PROGRESS", allowableValues = {"TODO", "IN_PROGRESS", "DONE", "CANCELLED"})
        String status,
        @Schema(description = "Task creation timestamp", example = "2024-01-15T10:30:00Z")
        Instant createdAt,
        @Schema(description = "ID of user who created the task", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID createdById,
        @Schema(description = "ID of user assigned to the task", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID assigneeId,
        @Schema(description = "ID of project this task belongs to", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID projectId,
        @Schema(description = "List of files attached to the task")
        List<FileResponseDTO> files
) {
    @Schema(description = "File attached to task")
    public record FileResponseDTO(
            @Schema(description = "File unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID id, 
            @Schema(description = "File name", example = "document.pdf")
            String name, 
            @Schema(description = "File download URL", example = "https://example.com/files/document.pdf")
            String url, 
            @Schema(description = "File upload timestamp", example = "2024-01-15T10:30:00Z")
            Instant uploadedAt
    ) {}
}
