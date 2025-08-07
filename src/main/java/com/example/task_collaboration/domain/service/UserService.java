// com.example.task_collaboration.domain.service.UserService.java
package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findByEmail(String email);
    void registerUser(User user);
    boolean existsByEmail(String email);

    Optional<User> findById(UUID userId);
}