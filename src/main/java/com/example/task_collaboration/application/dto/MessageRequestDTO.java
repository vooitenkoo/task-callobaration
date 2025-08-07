package com.example.task_collaboration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record MessageRequestDTO(
        @NotBlank(message = "Content is required")
        @Size(max = 1000, message = "Message content must not exceed 1000 characters")
        String content,
        UUID projectId
) {} 