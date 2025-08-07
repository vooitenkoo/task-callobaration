package com.example.task_collaboration.domain.service;
import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.application.mapper.TaskMapper;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.Task;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.TaskRepository;
import com.example.task_collaboration.domain.repository.ProjectMemberRepository;
import com.example.task_collaboration.domain.model.ProjectMember;
import com.example.task_collaboration.domain.model.File;
import com.example.task_collaboration.infrastructure.exсeption.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService; // Добавим зависимость
    private final UserService userService;
    private final MinioService minioService;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, UserService userService, MinioService minioService, ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.minioService = minioService;
        this.projectMemberRepository = projectMemberRepository;
    }

    @Transactional
    public TaskResponseDTO createTask(User currentUser, TaskRequestDTO dto) {
        Project project = projectService.findProjectByIdAndUser(dto.projectId(), currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Only project member can create tasks"));
        boolean canCreate = projectMemberRepository.findByUserIdAndProjectId(currentUser.getId(), project.getId())
            .stream().anyMatch(m -> m.getRole() == ProjectMember.ProjectRole.OWNER || m.getRole() == ProjectMember.ProjectRole.ADMIN);
        if (!canCreate) throw new AccessDeniedException("Only OWNER or ADMIN can create tasks");
        Task task = TaskMapper.toEntity(dto);
        task.setCreatedBy(currentUser);
        task.setProject(project);
        if (dto.assigneeId() != null) {
            task.setAssignee(userService.findById(dto.assigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found")));
        }
        // Работа с файлами
        if (dto.file() != null) {
            try {
                String url = minioService.uploadFile(dto.file());
                File file = new File();
                file.setName(dto.file().getOriginalFilename());
                file.setUrl(url);
                file.setTask(task);
                file.setUploadedBy(currentUser);
                file.setUploadedAt(java.time.Instant.now());
                task.setFiles(java.util.Collections.singletonList(file));
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
    public TaskResponseDTO getTaskById(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        // Проверяем, что пользователь является участником проекта
        boolean isMember = projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
            .stream().findFirst().isPresent();
        if (!isMember) {
            throw new AccessDeniedException("Access denied - not a project member");
        }
        return TaskMapper.toDto(task);
    }

    @Transactional
    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO dto, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        // Только OWNER или ADMIN может обновлять статус/дедлайн
        boolean canEdit = projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
            .stream().anyMatch(m -> m.getRole() == ProjectMember.ProjectRole.OWNER || m.getRole() == ProjectMember.ProjectRole.ADMIN);
        if (!canEdit) throw new AccessDeniedException("Only OWNER or ADMIN can update status or deadline");
        if (dto.status() != null || dto.deadline() != null) {
            TaskMapper.updateEntityFromDto(dto, task);
        } else {
            task.setTitle(dto.title());
            task.setDescription(dto.description());
            if (dto.assigneeId() != null) {
                task.setAssignee(userService.findById(dto.assigneeId())
                        .orElseThrow(() -> new RuntimeException("Assignee not found")));
            }
            // Работа с файлами
            if (dto.file() != null) {
                try {
                    String url = minioService.uploadFile(dto.file());
                    File file = new File();
                    file.setName(dto.file().getOriginalFilename());
                    file.setUrl(url);
                    file.setTask(task);
                    file.setUploadedBy(task.getCreatedBy());
                    file.setUploadedAt(java.time.Instant.now());
                    if (task.getFiles() == null) task.setFiles(new java.util.ArrayList<>());
                    task.getFiles().add(file);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload file: " + e.getMessage());
                }
            }
        }
        return TaskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        // Только OWNER может удалить
        boolean isOwner = projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
            .stream().anyMatch(m -> m.getRole() == ProjectMember.ProjectRole.OWNER);
        if (!isOwner) throw new AccessDeniedException("Only OWNER can delete task");
        taskRepository.delete(task);
    }
}