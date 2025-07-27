package com.example.task_collaboration.domain.repository;

import com.example.task_collaboration.domain.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    List<Task> findByProjectIdAndAssigneeId(UUID projectId, Long assigneeId);
}