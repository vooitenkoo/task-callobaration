package com.example.task_collaboration.infrastructure.controller;
import com.example.task_collaboration.application.dto.TaskRequestDTO;
import com.example.task_collaboration.application.dto.TaskResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.TaskService;
import com.example.task_collaboration.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    public TaskController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDTO createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute TaskRequestDTO dto) {
        User currentUser = userService.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return taskService.createTask(currentUser, dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TaskResponseDTO> getTasks(
            @RequestParam(required = false) UUID projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public TaskResponseDTO getTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return taskService.getTaskById(id, userDetails.getUser().getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.user.id == taskService.getTaskById(#id, #userDetails.user.id).project.createdById")
    public TaskResponseDTO updateTask(
            @PathVariable UUID id,
            @Valid @ModelAttribute TaskRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return taskService.updateTask(id, dto, userDetails.getUser().getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.user.id == taskService.getTaskById(#id, #userDetails.user.id).project.createdById")
    public void deleteTask(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUser().getId());
    }
}
