package com.example.task_collaboration.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponseDTO(
        UUID id,
        String name,
        String description,
        String status,
        Instant deadline,
        Long createdById,
        Long leadId
) {}