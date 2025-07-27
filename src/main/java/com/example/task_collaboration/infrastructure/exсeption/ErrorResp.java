package com.example.task_collaboration.infrastructure.exсeption;

public  class ErrorResp {
    private String message;

    public ErrorResp(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}