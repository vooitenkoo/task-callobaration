package com.example.task_collaboration.infrastructure.exсeption;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorResponse {
    private String message;
    private int status;
    private String timestamp;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = Instant.now().toString();
    }
    public ErrorResponse(String message){
        this.message = message;
    }

}