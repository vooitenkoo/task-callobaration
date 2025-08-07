package com.example.task_collaboration.application.dto;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        Instant deadline,
        String status,
        Instant createdAt,
        UUID createdById,
        UUID assigneeId,
        UUID projectId,
        List<FileResponseDTO> files
) {
    public record FileResponseDTO(UUID id, String name, String url, Instant uploadedAt) {}
}
