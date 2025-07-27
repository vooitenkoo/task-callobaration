package com.example.task_collaboration.infrastructure.controller;


import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.ProjectService;
import com.example.task_collaboration.domain.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponseDTO createProject(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProjectRequestDTO dto) {
        User currentUser = userService.findById(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return projectService.createProject(currentUser, dto);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponseDTO> getProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.getProjectsByUser(userDetails.getUser().getId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProjectResponseDTO getProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.getProjectById(id, userDetails.getUser().getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.user.id == projectService.getProjectById(#id, #userDetails.user.id).createdById")
    public ProjectResponseDTO updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.updateProject(id, dto, userDetails.getUser().getId());
    }

    @PutMapping("/{id}/lead")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.user.id == projectService.getProjectById(#id, #userDetails.user.id).createdById")
    public void assignLead(
            @PathVariable UUID id,
            @RequestBody Long newLeadId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.assignLead(id, newLeadId, userDetails.getUser().getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userDetails.user.id == projectService.getProjectById(#id, #userDetails.user.id).createdById")
    public void deleteProject(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.deleteProject(id, userDetails.getUser().getId());
    }
}