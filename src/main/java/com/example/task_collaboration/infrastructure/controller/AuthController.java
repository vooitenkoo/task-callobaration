package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.UserLoginDto;
import com.example.task_collaboration.application.dto.UserRegisterDto;
import com.example.task_collaboration.application.mapper.AuthResponse;
import com.example.task_collaboration.domain.service.AuthService;
import com.example.task_collaboration.infrastructure.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
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

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT tokens in cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, tokens in cookies"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or validation errors")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody UserLoginDto loginDto,
            BindingResult bindingResult,
            HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new AuthResponse(null, null));
        }
        return ResponseEntity.ok(authService.login(loginDto, response));
    }

    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token from cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(401).body(new AuthResponse(null, null));
        }
        try {
            return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new AuthResponse(null, null));
        }
    }

    @Operation(summary = "Logout", description = "Logout user and clear tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);
        authService.logout(refreshToken, response);
        return ResponseEntity.ok("Logout successful");
    }
}
