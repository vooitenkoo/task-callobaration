package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
@Data
public class ProfileRequestDTO {
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Schema(description = "User bio", example = "Software developer with 5 years experience")
    private String bio;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Schema(description = "User location", example = "New York, USA")
    private String location;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Schema(description = "User job title", example = "Senior Developer")
    private String jobTitle;

    @Schema(type = "string", format = "binary", description = "Avatar image file")
    private MultipartFile avatarFile;
}