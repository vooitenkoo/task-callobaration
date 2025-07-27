package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.UserLoginDto;
import com.example.task_collaboration.application.dto.UserRegisterDto;
import com.example.task_collaboration.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserMapper {

    public User toEntity(UserRegisterDto registerDto) {
        if (registerDto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(registerDto.getEmail());
        user.setPassword(registerDto.getPassword());
        user.setName(registerDto.getName());
        user.setRole(User.Role.USER);
        user.setBlocked(false);
        user.setCreatedAt(java.time.Instant.now());
        return user;
    }

    public UserRegisterDto toRegisterDto(User user) {
        if (user == null) {
            return null;
        }
        UserRegisterDto dto = new UserRegisterDto();
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());

        return dto;
    }

    public User toEntity(UserLoginDto loginDto) {
        if (loginDto == null) {
            return null;
        }
        User user = new User();
        user.setEmail(loginDto.getEmail());
        user.setPassword(loginDto.getPassword());
        return user;
    }

    public UserLoginDto toLoginDto(User user) {
        if (user == null) {
            return null;
        }
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail(user.getEmail());
        return dto;
    }
}