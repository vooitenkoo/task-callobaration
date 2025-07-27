package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.domain.model.Project;
import java.time.Instant;

public class ProjectMapper {

    public static Project toEntity(ProjectRequestDTO dto) {
        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setDeadline(dto.deadline());
        project.setCreatedAt(Instant.now());
        project.setStatus(Project.Status.ACTIVE); // Установим дефолтное значение
        return project;
    }

    public static ProjectResponseDTO toDto(Project entity) {
        return new ProjectResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getDeadline(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null // Временная логика для leadId
        );
    }

    public static void updateEntityFromDto(ProjectRequestDTO dto, Project entity) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setDeadline(dto.deadline());
    }
}