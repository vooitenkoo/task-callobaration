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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
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


        if (dto.file() != null) {
            CompletableFuture<String> uploadFuture = uploadFileAsync(dto.file(), currentUser);
            String url = uploadFuture.join();
            File file = new File();
            file.setName(dto.file().getOriginalFilename());
            file.setUrl(url);
            file.setTask(task);
            file.setUploadedBy(currentUser);
            file.setUploadedAt(java.time.Instant.now());
            task.setFiles(java.util.Collections.singletonList(file));
        }

        return TaskMapper.toDto(taskRepository.save(task));
    }

    public CompletableFuture<String> uploadFileAsync(MultipartFile file, User currentUser) {
        try {
            String url = minioService.uploadFile(file);
            return CompletableFuture.completedFuture(url);
        } catch (Exception e) {
            CompletableFuture<String> failed = new CompletableFuture<>();
            failed.completeExceptionally(
                    new RuntimeException("Failed to upload file: " + e.getMessage(), e)
            );
            return failed;
        }
    }


    @Transactional(readOnly = true)
    public CompletableFuture<List<TaskResponseDTO>> getTasksByProject(UUID projectId) {
        return CompletableFuture.supplyAsync(() -> taskRepository.findByProjectId(projectId).stream()
                .map(TaskMapper::toDto)
                .toList());
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
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

        boolean canEdit = projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
                .stream()
                .anyMatch(m -> m.getRole() == ProjectMember.ProjectRole.OWNER
                        || m.getRole() == ProjectMember.ProjectRole.ADMIN);

        if (!canEdit) {
            throw new AccessDeniedException("Only OWNER or ADMIN can update status or deadline");
        }

        // 1. обновляем поля из DTO (title, description, status, deadline)
        TaskMapper.updateEntityFromDto(dto, task);

        // 2. обновляем assignee, если пришёл
        if (dto.assigneeId() != null) {
            task.setAssignee(userService.findById(dto.assigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found")));
        }

        // 3. добавляем файл, если он пришёл
        if (dto.file() != null) {
            User uploader = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String url = uploadFileAsync(dto.file(), uploader).join();

            com.example.task_collaboration.domain.model.File file = new com.example.task_collaboration.domain.model.File();
            file.setName(dto.file().getOriginalFilename());
            file.setUrl(url);
            file.setTask(task);
            file.setUploadedBy(uploader);
            file.setUploadedAt(java.time.Instant.now());

            if (task.getFiles() == null) {
                task.setFiles(new java.util.ArrayList<>());
            }
            task.getFiles().add(file);
        }

        return TaskMapper.toDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(UUID id, UUID userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        boolean isOwner = projectMemberRepository.findByUserIdAndProjectId(userId, task.getProject().getId())
                .stream().anyMatch(m -> m.getRole() == ProjectMember.ProjectRole.OWNER);
        if (!isOwner) throw new AccessDeniedException("Only OWNER can delete task");
        taskRepository.delete(task);
    }
}