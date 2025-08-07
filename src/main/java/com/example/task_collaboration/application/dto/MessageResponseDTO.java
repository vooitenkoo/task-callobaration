package com.example.task_collaboration.application.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDTO(
        UUID id,
        String content,
        Instant sentAt,
        boolean isRead,
        UUID projectId,
        SenderDTO sender
) {
    public record SenderDTO(
            UUID id,
            String name
    ) {}
} 