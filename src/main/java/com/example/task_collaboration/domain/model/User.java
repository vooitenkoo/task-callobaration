package com.example.task_collaboration.domain.model;

import com.example.task_collaboration.domain.model.ProjectMember;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    private String password;

    @Transient
    private String confirmPassword;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Username cannot be empty")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Username must contain only Latin letters")
    @Length(max = 100, message = "Username must not exceed 100 characters")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Role cannot be null")
    private Role role;

    @Column(nullable = false)
    @NotNull(message = "Blocked status cannot be null")
    private boolean isBlocked = false;

    @Column(nullable = false)
    @NotNull(message = "Created at cannot be null")
    private Instant createdAt = Instant.now();

    // OAuth2 fields
    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ProjectMember> projectMemberships;

    public enum Role {
        USER, ADMIN
    }

    public enum AuthProvider {
        LOCAL, GOOGLE, GITHUB
    }

    // Helper methods for OAuth2
    public boolean isOAuth2User() {
        return provider != null && provider != AuthProvider.LOCAL;
    }

    public boolean isLocalUser() {
        return provider == null || provider == AuthProvider.LOCAL;
    }
}