package com.example.task_collaboration.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "User registration request")
public class UserRegisterDto {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "Email must be a valid address")
    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one letter, one number, and no special characters except @$!%*?&")
    @Schema(description = "User password (min 8 chars, must contain letters and numbers)", example = "password123")
    private String password;

    @NotBlank(message = "Confirm password cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Confirm password must match password format")
    @Schema(description = "Password confirmation", example = "password123")
    private String confirmPassword;

    @NotBlank(message = "Username cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Username must contain only Latin letters")
    @Schema(description = "User name (Latin letters only)", example = "JohnDoe")
    private String name;
}