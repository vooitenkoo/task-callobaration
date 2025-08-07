package com.example.task_collaboration.infrastructure.exсeption;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(UUID userId) {
        super("Profile not found for user ID: " + userId);
    }
}
