package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.domain.model.Task;
import java.time.Instant;

public class TaskMapper {

    public static Task toEntity(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDeadline(dto.deadline());
        task.setStatus(dto.status() != null ? Task.TaskStatus.valueOf(dto.status()) : Task.TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        task.setProject(dto.projectId());
        return task;
    }

    public static TaskResponseDTO toDto(Task entity) {
        return new TaskResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDeadline(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getFileUrl(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.getAssignee() != null ? entity.getAssignee().getId() : null,
                entity.getProject() != null ? entity.getProject().getId() : null
        );
    }

    public static void updateEntityFromDto(TaskRequestDTO dto, Task entity) {
        entity.setTitle(dto.title());
        entity.setDescription(dto.description());
        entity.setDeadline(dto.deadline());
        if (dto.status() != null) entity.setStatus(Task.TaskStatus.valueOf(dto.status()));
        if (dto.deadline() != null) entity.setDeadline(dto.deadline());
    }
}