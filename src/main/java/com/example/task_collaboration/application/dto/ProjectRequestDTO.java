package com.example.task_collaboration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProjectRequestDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        Instant deadline,
        List<ProjectMemberDTO> members
) {
    public record ProjectMemberDTO(UUID userId, String role) {}
}