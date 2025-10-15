package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.UserLoginDto;
import com.example.task_collaboration.application.dto.UserRegisterDto;
import com.example.task_collaboration.application.mapper.AuthResponse;
import com.example.task_collaboration.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or validation errors")
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody UserRegisterDto registerDto,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
        }
        authService.register(registerDto);
        return ResponseEntity.ok("User registered successfully");
    }

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns access and refresh tokens"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or validation errors")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginDto loginDto,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null));
        }
        return ResponseEntity.ok(authService.login(loginDto));
    }
}
