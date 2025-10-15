package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import java.util.List;

@Schema(description = "Project response data")
public record ProjectResponseDTO(
        @Schema(description = "Project unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        @Schema(description = "Project name", example = "Task Management System")
        String name,
        @Schema(description = "Project description", example = "A comprehensive task management and collaboration platform")
        String description,
        @Schema(description = "Project status", example = "ACTIVE", allowableValues = {"ACTIVE", "COMPLETED", "CANCELLED", "ON_HOLD"})
        String status,
        @Schema(description = "Project creation timestamp", example = "2024-01-15T10:30:00Z")
        Instant createdAt,
        @Schema(description = "Project deadline", example = "2024-12-31T23:59:59Z")
        Instant deadline,
        @Schema(description = "ID of user who created the project", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID createdById,
        @Schema(description = "ID of project lead", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID leadId,
        @Schema(description = "List of project members")
        List<ProjectMemberDTO> members
) {
    @Schema(description = "Project member information")
    public record ProjectMemberDTO(
            @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID userId, 
            @Schema(description = "Username", example = "john_doe")
            String username, 
            @Schema(description = "Member role", example = "DEVELOPER", allowableValues = {"ADMIN", "LEAD", "DEVELOPER", "VIEWER"})
            String role
    ) {}
}