package com.example.task_collaboration.domain.repository;

import com.example.task_collaboration.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByCreatedById(Long userId);
    Project findByIdAndCreatedById(UUID id, Long userId);
}