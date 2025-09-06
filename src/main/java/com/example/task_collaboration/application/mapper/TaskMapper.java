package com.example.task_collaboration.application.mapper;

import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.domain.model.Task;
import com.example.task_collaboration.domain.model.File;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class TaskMapper {

    public static Task toEntity(TaskRequestDTO dto) {
        Task task = new Task();
        task.setTitle(dto.title());
        task.setDescription(dto.description());
        task.setDeadline(dto.deadline());
        task.setStatus(dto.status() != null ? Task.TaskStatus.valueOf(dto.status()) : Task.TaskStatus.PENDING);
        task.setCreatedAt(Instant.now());
        // projectId будет установлен в сервисе
        return task;
    }

    public static TaskResponseDTO toDto(Task entity) {
        return new TaskResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDeadline(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getCreatedAt(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getId() : null,
                entity.getAssignee() != null ? entity.getAssignee().getId() : null,
                entity.getProject() != null ? entity.getProject().getId() : null,
                entity.getFiles() != null ? entity.getFiles().stream().map(TaskMapper::toFileDto).collect(Collectors.toList()) : null
        );
    }

    public static TaskResponseDTO.FileResponseDTO toFileDto(File file) {
        return new TaskResponseDTO.FileResponseDTO(
            file.getId(),
            file.getName(),
            file.getUrl(),
            file.getUploadedAt()
        );
    }

    public static void updateEntityFromDto(TaskRequestDTO dto, Task entity) {
        if (dto.title() != null) {
            entity.setTitle(dto.title());
        }
        if (dto.description() != null) {
            entity.setDescription(dto.description());
        }
        if (dto.status() != null) {
            entity.setStatus(Task.TaskStatus.valueOf(dto.status()));
        }
        if (dto.deadline() != null) {
            entity.setDeadline(dto.deadline());
        }
    }

}