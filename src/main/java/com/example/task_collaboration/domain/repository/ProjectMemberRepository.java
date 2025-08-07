package com.example.task_collaboration.domain.repository;

import com.example.task_collaboration.domain.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    List<ProjectMember> findByUserId(UUID userId);
    List<ProjectMember> findByProjectId(UUID projectId);
    List<ProjectMember> findByProjectIdAndRole(UUID projectId, ProjectMember.ProjectRole role);
    List<ProjectMember> findByUserIdAndProjectId(UUID userId, UUID projectId);
} 