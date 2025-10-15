package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Project creation/update request")
public record ProjectRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        @Schema(description = "Project name", example = "Task Management System")
        String name,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        @Schema(description = "Project description", example = "A comprehensive task management and collaboration platform")
        String description,
        @Schema(description = "Project deadline", example = "2024-12-31T23:59:59Z")
        Instant deadline,
        @Schema(description = "List of project members")
        List<ProjectMemberDTO> members
) {
    @Schema(description = "Project member information")
    public record ProjectMemberDTO(
            @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID userId, 
            @Schema(description = "Member role", example = "DEVELOPER", allowableValues = {"ADMIN", "LEAD", "DEVELOPER", "VIEWER"})
            String role
    ) {}
}