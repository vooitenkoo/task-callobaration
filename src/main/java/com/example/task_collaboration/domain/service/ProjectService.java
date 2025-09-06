package com.example.task_collaboration.domain.service;

import com.example.task_collaboration.application.dto.ProjectRequestDTO;
import com.example.task_collaboration.application.dto.ProjectResponseDTO;
import com.example.task_collaboration.application.mapper.ProjectMapper;
import com.example.task_collaboration.domain.model.Project;
import com.example.task_collaboration.domain.model.ProjectMember;
import com.example.task_collaboration.domain.model.User;
import com.example.task_collaboration.domain.repository.ProjectMemberRepository;
import com.example.task_collaboration.domain.repository.ProjectRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, ProjectMemberRepository projectMemberRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.userService = userService;
    }

    @Transactional
    public ProjectResponseDTO createProject(User currentUser, ProjectRequestDTO dto) {
        Project project = ProjectMapper.toEntity(dto);
        project.setCreatedBy(currentUser);
        Project savedProject = projectRepository.save(project);

        ProjectMember ownerMember = new ProjectMember();
        ownerMember.setUser(currentUser);
        ownerMember.setProject(savedProject);
        ownerMember.setRole(ProjectMember.ProjectRole.OWNER);
        projectMemberRepository.save(ownerMember);

        if (dto.members() != null) {
            addMembersAsync(savedProject, dto.members(), currentUser.getId());
        }

        return ProjectMapper.toDto(savedProject);
    }

    @Async // Многопоточность: выполняется в отдельном потоке
    public CompletableFuture<Void> addMembersAsync(Project project, List<ProjectRequestDTO.ProjectMemberDTO> members, UUID creatorId) {
        return CompletableFuture.runAsync(() -> {
            members.stream()
                    .filter(m -> m.userId() != null && !m.userId().equals(creatorId))
                    .forEach(m -> {
                        try {
                            User memberUser = userService.findById(m.userId())
                                    .orElseThrow(() -> new RuntimeException("User not found: " + m.userId()));
                            ProjectMember member = new ProjectMember();
                            member.setUser(memberUser);
                            member.setProject(project);
                            member.setRole(ProjectMember.ProjectRole.valueOf(m.role()));
                            projectMemberRepository.save(member);
                        } catch (Exception e) {
                            System.err.println("Failed to add member: " + e.getMessage());
                        }
                    });
        });
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsByUser(UUID userId) {
        return projectMemberRepository.findByUserId(userId).stream()
                .map(pm -> ProjectMapper.toDto(pm.getProject()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> getProjectsCreatedByUser(UUID userId) {
        return projectRepository.findByCreatedById(userId).stream()
                .map(ProjectMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO getProjectById(UUID id, UUID userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        boolean isMember = projectMemberRepository.findByUserIdAndProjectId(userId, project.getId()).size() > 0;
        if (!isMember) throw new RuntimeException("Access denied");
        return ProjectMapper.toDto(project);
    }

    @Transactional
    public Optional<Project> findProjectByIdAndUser(UUID id, UUID userId) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project == null) return Optional.empty();
        boolean isMember = projectMemberRepository.findByUserIdAndProjectId(userId, project.getId()).size() > 0;
        return isMember ? Optional.of(project) : Optional.empty();
    }

    @Transactional
    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO dto, UUID userId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(userId)) {
            throw new RuntimeException("Only LEAD can update project");
        }
        ProjectMapper.updateEntityFromDto(dto, project);
        projectRepository.save(project);

        List<ProjectMember> currentMembers = projectMemberRepository.findByProjectId(id);
        List<ProjectRequestDTO.ProjectMemberDTO> newMembers = dto.members() != null ? dto.members() : List.of();

        for (ProjectRequestDTO.ProjectMemberDTO m : newMembers) {
            ProjectMember.ProjectRole newRole = ProjectMember.ProjectRole.valueOf(m.role());
            ProjectMember existing = currentMembers.stream()
                    .filter(pm -> pm.getUser().getId().equals(m.userId()))
                    .findFirst().orElse(null);
            if (existing != null) {
                if (!existing.getRole().equals(newRole)) {
                    existing.setRole(newRole);
                    projectMemberRepository.save(existing);
                }
            } else {
                User memberUser = userService.findById(m.userId())
                        .orElseThrow(() -> new RuntimeException("User not found: " + m.userId()));
                ProjectMember member = new ProjectMember();
                member.setUser(memberUser);
                member.setProject(project);
                member.setRole(newRole);
                projectMemberRepository.save(member);
            }
        }
        return ProjectMapper.toDto(project);
    }

    @Transactional
    public void assignLead(UUID id, UUID newLeadId, UUID currentUserId) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(currentUserId)) {
            throw new RuntimeException("Only LEAD can assign new LEAD");
        }
        User newLead = userService.findById(newLeadId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        project.setCreatedBy(newLead);
    }

    @Async // Многопоточность: выполняется в отдельном потоке
    @Transactional
    public CompletableFuture<Void> deleteProjectAsync(UUID id, UUID userId) {
        return CompletableFuture.runAsync(() -> {
            Project project = projectRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Project not found"));
            if (!project.getCreatedBy().getId().equals(userId)) {
                throw new RuntimeException("Only LEAD can delete project");
            }
            projectRepository.delete(project);
        });
    }

    @Transactional
    public void removeProjectMember(UUID projectId, UUID memberId, UUID currentUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!project.getCreatedBy().getId().equals(currentUserId)) {
            throw new RuntimeException("Only LEAD can remove members");
        }
        List<ProjectMember> members = projectMemberRepository.findByProjectId(projectId);
        ProjectMember toRemove = members.stream()
                .filter(pm -> pm.getUser().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Member not found in project"));
        if (toRemove.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new RuntimeException("Cannot remove project owner");
        }
        projectMemberRepository.delete(toRemove);
    }
}