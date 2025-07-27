package com.example.task_collaboration.application.dto;

import java.time.Instant;
import java.util.UUID;

public record TaskResponseDTO(
        UUID id,
        String title,
        String description,
        Instant deadline,
        String status,
        String fileUrl,
        Long createdById,
        Long assigneeId,
        UUID projectId
) {}
