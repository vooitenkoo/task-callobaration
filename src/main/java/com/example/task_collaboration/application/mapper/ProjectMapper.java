package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.ProjectMember;
import com.example.task_collaboration.domain.model.User;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.List;

public class ProjectMapper {

    public static Project toEntity(ProjectRequestDTO dto) {
        Project project = new Project();
        project.setName(dto.name());
        project.setDescription(dto.description());
        project.setDeadline(dto.deadline());
        project.setCreatedAt(Instant.now());
        project.setStatus(Project.Status.ACTIVE); // Установим дефолтное значение
        // Убираем создание ProjectMember здесь - это будет делаться в сервисе с полными объектами User
        return project;
    }

    public static ProjectResponseDTO toDto(Project entity) {
        return new ProjectResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getDeadline(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null, // Временная логика для leadId
                entity.getMembers() != null ? entity.getMembers().stream().map(ProjectMapper::toMemberDto).collect(Collectors.toList()) : null
        );
    }

    public static ProjectResponseDTO.ProjectMemberDTO toMemberDto(ProjectMember member) {
        return new ProjectResponseDTO.ProjectMemberDTO(
            member.getUser().getId(),
            member.getUser().getName(),
            member.getRole().name()
        );
    }

    public static void updateEntityFromDto(ProjectRequestDTO dto, Project entity) {
        entity.setName(dto.name());
        entity.setDescription(dto.description());
        entity.setDeadline(dto.deadline());
        // Убираем создание ProjectMember здесь - это будет делаться в сервисе с полными объектами User
    }
}