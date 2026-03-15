package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.infrastructure.config.JwtTokenProvider;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JwtService {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtService(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateAccessToken(user.getEmail(), user.getId());
    }
    
    public String generateRefreshToken(User user) {
        return jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getId());
    }
    
    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }
    
    public UUID getUserIdFromToken(String token) {
        return jwtTokenProvider.getUserIdFromToken(token);
    }
    
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    public boolean isTokenExpired(String token) {
        return jwtTokenProvider.isTokenExpired(token);
    }
}
