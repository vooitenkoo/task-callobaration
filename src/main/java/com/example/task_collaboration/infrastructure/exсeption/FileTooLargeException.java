package com.example.task_collaboration.infrastructure.exсeption;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }
}
