package com.example.task_collaboration.domain.repository;// com.example.task_collaboration.infrastructure.repository.ProfileRepository.java

import com.example.task_collaboration.domain.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findById(Long id);
    Optional<Profile> findByUserId(Long userId);
}