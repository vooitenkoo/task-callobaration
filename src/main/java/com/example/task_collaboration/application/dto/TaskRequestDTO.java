package com.example.task_collaboration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Task creation/update request")
public record TaskRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        @Schema(description = "Task title", example = "Implement user authentication")
        String title,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(description = "Task description", example = "Add JWT-based authentication to the API")
        String description,
        @Schema(description = "Task deadline", example = "2024-12-31T23:59:59Z")
        Instant deadline,
        @Schema(description = "Task status", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "DONE", "CANCELLED"})
        String status,
        @Schema(type = "string", format = "binary", description = "File to upload with the task")
        MultipartFile file,
        @Schema(description = "ID of user assigned to the task", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID assigneeId,
        @Schema(description = "ID of project this task belongs to", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID projectId
) {}