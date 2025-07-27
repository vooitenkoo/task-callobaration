package com.example.task_collaboration.infrastructure.exсeption;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long userId) {
    }
}
