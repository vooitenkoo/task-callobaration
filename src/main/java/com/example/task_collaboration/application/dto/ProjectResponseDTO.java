package com.example.task_collaboration.application.dto;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record ProjectResponseDTO(
        UUID id,
        String name,
        String description,
        String status,
        Instant createdAt,
        Instant deadline,
        UUID createdById,
        UUID leadId,
        List<ProjectMemberDTO> members
) {
    public record ProjectMemberDTO(UUID userId, String username, String role) {}
}