package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.UserLoginDto;
import com.example.task_collaboration.application.dto.UserRegisterDto;
import com.example.task_collaboration.application.mapper.AuthResponse;
import com.example.task_collaboration.application.mapper.UserMapper;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.infrastructure.config.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
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
        user.setConfirmPassword(null); // Очищаем временное поле перед сохранением
        userService.registerUser(user);
    }

    public AuthResponse login(UserLoginDto loginDto) {
        User user = userService.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(loginDto.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(loginDto.getEmail());
        return new AuthResponse(accessToken, refreshToken);
    }
}