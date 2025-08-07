package com.example.task_collaboration.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record TaskRequestDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,
        Instant deadline,
        String status,
        @Schema(type = "string", format = "binary", description = "File to upload")
        MultipartFile file,
        UUID assigneeId,
        UUID projectId
) {}