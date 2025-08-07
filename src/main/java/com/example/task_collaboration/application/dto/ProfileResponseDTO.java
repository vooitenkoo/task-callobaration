package com.example.task_collaboration.application.dto;

import java.util.UUID;

public record ProfileResponseDTO(
        UUID id,
        String displayName,
        String avatarUrl,
        String bio,
        String location,
        String jobTitle
) {}