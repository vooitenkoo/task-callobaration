package com.example.task_collaboration.domain.service;
import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.application.mapper.ProjectMapper;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectResponseDTO createProject(User currentUser, ProjectRequestDTO dto) {
        Project project = ProjectMapper.toEntity(dto);
        project.setCreatedBy(currentUser);
        Project savedProject = projectRepository.save(project);
        return ProjectMapper.toDto(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByUser(Long userId) {
        return projectRepository.findByCreatedById(userId).stream()
                .map(ProjectMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(UUID id, Long userId) {
        Project project = projectRepository.findByIdAndCreatedById(id, userId);
        return ProjectMapper.toDto(project);
    }
    @Transactional
    public Optional<Project> findProjectByIdAndUser(UUID id, Long userId) {
        return Optional.ofNullable(projectRepository.findByIdAndCreatedById(id, userId));
    }
    @Transactional
    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO dto, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only LEAD can update project");
        }
        ProjectMapper.updateEntityFromDto(dto, project);
        return ProjectMapper.toDto(projectRepository.save(project));
    }

    @Transactional
    public void assignLead(UUID id, Long newLeadId, Long currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(currentUserId)) {
            throw new RuntimeException("Only LEAD can assign new LEAD");
        }
        User newLead = userService.findById(newLeadId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setCreatedBy(newLead); // Временная логика
    }

    @Transactional
    public void deleteProject(UUID id, Long userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only LEAD can delete project");
        }
        projectRepository.delete(project);
    }
}