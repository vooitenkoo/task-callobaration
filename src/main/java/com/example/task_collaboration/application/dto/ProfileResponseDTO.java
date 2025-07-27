package com.example.task_collaboration.application.dto;

import lombok.Data;

@Data
public class ProfileResponseDTO {
    private Long id;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String location;
    private String jobTitle;
}