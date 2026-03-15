package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.UserLoginDto;
import com.example.task_collaboration.application.dto.UserRegisterDto;
import com.example.task_collaboration.application.mapper.AuthResponse;
import com.example.task_collaboration.application.mapper.UserMapper;
import com.example.task_collaboration.domain.model.RefreshToken;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.infrastructure.config.JwtTokenProvider;
import com.example.task_collaboration.infrastructure.service.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;
    private final CookieService cookieService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, UserMapper userMapper,
                       RefreshTokenService refreshTokenService, CookieService cookieService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.refreshTokenService = refreshTokenService;
        this.cookieService = cookieService;
    }

    @Transactional
    public void register(UserRegisterDto registerDto) {
        if (userService.existsByEmail(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (!registerDto.getPassword().equals(registerDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        User user = userMapper.toEntity(registerDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setConfirmPassword(null);
        userService.registerUser(user);
    }

    public AuthResponse login(UserLoginDto loginDto, HttpServletResponse response) {
        User user = userService.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Revoke old refresh tokens
        refreshTokenService.revokeAllUserTokens(user);
        
        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(loginDto.getEmail(), user.getId());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        
        // Set cookies
        cookieService.setTokenCookies(response, accessToken, refreshToken.getToken());
        
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse refreshToken(String refreshTokenValue, HttpServletResponse response) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        refreshTokenService.verifyExpiration(refreshToken);
        
        User user = refreshToken.getUser();
        
        // Revoke old refresh token
        refreshTokenService.revokeToken(refreshTokenValue);
        
        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getId());
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
        
        // Set cookies
        cookieService.setTokenCookies(response, accessToken, newRefreshToken.getToken());
        
        return new AuthResponse(accessToken, newRefreshToken.getToken());
    }

    public void logout(String refreshTokenValue, HttpServletResponse response) {
        if (refreshTokenValue != null) {
            refreshTokenService.revokeToken(refreshTokenValue);
        }
        cookieService.clearTokenCookies(response);
    }
}
