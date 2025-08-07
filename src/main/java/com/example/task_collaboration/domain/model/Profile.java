package com.example.task_collaboration.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
public class Profile {
    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Size(max = 100, message = "Display name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String displayName;

    @Size(max = 1000, message = "Avatar URL must not exceed 1000 characters")
    @Column(length = 1000)
    private String avatarUrl;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Column(length = 1000)
    private String bio;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    @Column(length = 255)
    private String location;

    @Size(max = 100, message = "Job title must not exceed 100 characters")
    @Column(length = 100)
    private String jobTitle;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();
}