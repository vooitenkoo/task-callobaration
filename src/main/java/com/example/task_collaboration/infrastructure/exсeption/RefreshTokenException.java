package com.example.task_collaboration.infrastructure.exсeption;

public class RefreshTokenException extends RuntimeException {
    public RefreshTokenException(String message) {
        super(message);
    }
}
