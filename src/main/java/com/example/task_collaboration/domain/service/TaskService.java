package com.example.task_collaboration.domain.service;
import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.application.mapper.TaskMapper;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.Task;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.TaskRepository;
import com.example.task_collaboration.infrastructure.exсeption.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService; // Добавим зависимость
    private final UserService userService;
    private final MinioService minioService;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, UserService userService, MinioService minioService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.minioService = minioService;
    }

    @Transactional
    public TaskResponseDTO createTask(User currentUser, TaskRequestDTO dto) {
        Project project = projectService.findProjectByIdAndUser(dto.projectId(), currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Only LEAD can create tasks"));
        Task task = TaskMapper.toEntity(dto);
        task.setCreatedBy(currentUser);
        task.setProject(project);
        if (dto.assigneeId() != null) {
            task.setAssignee(userService.findById(dto.assigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found")));
        }
        if (dto.file() != null) {
            try {
                task.setFileUrl(minioService.uploadAvatar(dto.file()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file: " + e.getMessage());
            }
        }
        return TaskMapper.toDto(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(TaskMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(UUID id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getProject().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }
        return TaskMapper.toDto(task);
    }

    @Transactional
    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO dto, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getProject().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Only LEAD can update status or deadline");
        }
        if (dto.status() != null || dto.deadline() != null) {
            TaskMapper.updateEntityFromDto(dto, task);
        } else {
            task.setTitle(dto.title());
            task.setDescription(dto.description());
            if (dto.assigneeId() != null) {
                task.setAssignee(userService.findById(dto.assigneeId())
                        .orElseThrow(() -> new RuntimeException("Assignee not found")));
            }
            if (dto.file() != null) {
                try {
                    task.setFileUrl(minioService.uploadAvatar(dto.file()));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload file: " + e.getMessage());
                }
            }
        }
        return TaskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getProject().getCreatedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Only LEAD can delete task");
        }
        taskRepository.delete(task);
    }
}