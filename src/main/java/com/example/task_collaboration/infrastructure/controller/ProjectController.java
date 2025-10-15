package com.example.task_collaboration.infrastructure.controller;

import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.service.CustomUserDetails;
import com.example.task_collaboration.domain.service.ProjectService;
import com.example.task_collaboration.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new project", description = "Creates a new project with members")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
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

    @Operation(summary = "Get user projects", description = "Retrieves all projects where the user is a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponseDTO> getProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.getProjectsByUser(userDetails.getUser().getId());
    }

    @Operation(summary = "Get projects created by user", description = "Retrieves all projects created by the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/created")
    @PreAuthorize("isAuthenticated()")
    public List<ProjectResponseDTO> getProjectsCreatedByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.getProjectsCreatedByUser(userDetails.getUser().getId());
    }

    @Operation(summary = "Get project by ID", description = "Retrieves a specific project by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProjectResponseDTO getProject(
            @Parameter(description = "Project ID")
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.getProjectById(id, userDetails.getUser().getId());
    }

    @Operation(summary = "Update project", description = "Updates an existing project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ProjectResponseDTO updateProject(
            @Parameter(description = "Project ID")
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.updateProject(id, dto, userDetails.getUser().getId());
    }

    @Operation(summary = "Assign project lead", description = "Assigns a new lead to the project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lead assigned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    @PutMapping("/{id}/assign-lead")
    @PreAuthorize("isAuthenticated()")
    public void assignLead(
            @Parameter(description = "Project ID")
            @PathVariable UUID id,
            @RequestBody UUID newLeadId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.assignLead(id, newLeadId, userDetails.getUser().getId());
    }

    @Operation(summary = "Delete project", description = "Deletes a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public CompletableFuture<Void> deleteProject(
            @Parameter(description = "Project ID")
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return projectService.deleteProjectAsync(id, userDetails.getUser().getId());
    }

    @Operation(summary = "Remove project member", description = "Removes a member from the project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member removed successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("isAuthenticated()")
    public void removeProjectMember(
            @Parameter(description = "Project ID")
            @PathVariable UUID projectId,
            @Parameter(description = "User ID")
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.removeProjectMember(projectId, userId, userDetails.getUser().getId());
    }
}